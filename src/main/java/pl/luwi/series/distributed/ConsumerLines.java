package pl.luwi.series.distributed;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;

import static pl.luwi.series.distributed.Constants.*;
import static pl.luwi.series.reducer.Stopwatch.*;
import org.apache.activemq.ActiveMQConnectionFactory;

import pl.luwi.series.distributed.TaskOrderedLine.RDPresult;
import pl.luwi.series.distributed.TaskSearch.SearchResult;

public class ConsumerLines implements Runnable, IStoppable {

	// not threadsafe
	Session session;
	MessageProducer result_producer;
	MessageProducer line_producer;
	MessageConsumer line_consumer;

	int id = 0;

	RegistrationService registrar;

	static ConsumerLinesSettings settings;
	static ReentrantLock epsilonlock = new ReentrantLock();
	static ConcurrentHashMap<Integer, Double> epsilonStore = new ConcurrentHashMap<>();

	static ReentrantLock lineIdlock = new ReentrantLock();
	static Stack<Integer> validLineIDs = new Stack<>();

	ExecutorService executor;

	public ConsumerLines(int id, ExecutorService executor, RegistrationService registrar, Connection connection,
			Destination lines, Destination results) throws JMSException, RemoteException {
		this.id = id;
		this.executor = executor;
		this.registrar = registrar;
		this.session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		this.line_consumer = session.createConsumer(lines);
		this.line_producer = session.createProducer(lines);
		this.result_producer = session.createProducer(results);
	}

	Stack<TaskOrderedLine<?>> self = new Stack<>();
	
	public void process() throws JMSException, RemoteException, InterruptedException {

		TaskOrderedLine<?> line = null;
		if (!self.isEmpty()) {
			line = self.pop();
		} else {
			Message received = null;
			try {
				received = line_consumer.receive();
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (received == null) {
				return;
			}
			if (!(received instanceof ObjectMessage)) {
				System.err.println(" not an objectmessage!:\n" + received.toString());
			}
			line = (TaskOrderedLine<?>) ((ObjectMessage) received).getObject();
		}

		epsilonlock.lock();
		Double epsilon = epsilonStore.get(line.RDPID);
		if (epsilon == null) {
			epsilon = registrar.getEpsilon(line.RDPID);
			epsilonStore.put(line.RDPID, epsilon);
		}
		epsilonlock.unlock();

		if (line.points.size() > settings.COMPUTATION_THRESHOLD_SIZESPLIT) {
			// pass around the line if large
			RDPresult result = RDP(line, epsilon);
			if (result.points != null) {
				LineResult lineresult = new LineResult(line.RDPID, line.lineID, result.points);
				result_producer.send(session.createObjectMessage( lineresult));
				System.out.printf("%d:%d:%d result\n", id, line.RDPID, line.lineID);
			} else {
				List<TaskOrderedLine<?>> lines = result.lines;
				lines.sort(biggestFirst);
				TaskOrderedLine<?> bigger = lines.get(0);
				TaskOrderedLine<?> smaller = lines.get(1);

				bigger.lineID = line.lineID;
				self.push(bigger);

				lineIdlock.lock();
				if (validLineIDs.empty()) {
					validLineIDs.addAll(registrar.getLineIDs());
				}
				smaller.lineID = validLineIDs.pop();
				lineIdlock.unlock();

				registrar.submitExpectation(line.RDPID, smaller.lineID);
				line_producer.send(session.createObjectMessage(smaller));
				System.out.printf("%d:%d:%d signal\n", id, line.RDPID, line.lineID);

			}
		} else {
			// solve completely if small
			Stack<TaskOrderedLine<?>> tempstack = new Stack<>();
			List<OrderedPoint> returnablePoints = new ArrayList<>();
			tempstack.push(line);
			while (!tempstack.empty()) {
				TaskOrderedLine<?> templine = tempstack.pop();
				RDPresult result = RDP(templine, epsilon);
				if (result.points != null) {
					for (OrderedPoint orderedPoint : (List<? extends OrderedPoint>) result.points) {
						returnablePoints.add(orderedPoint);
					}
				} else {
					List<TaskOrderedLine<?>> lines = result.lines;
					for (TaskOrderedLine<?> orderedLine : lines) {
						tempstack.push(orderedLine);
					}
				}
			}
			returnablePoints = returnablePoints.stream().distinct().collect(Collectors.toList());
			LineResult result = new LineResult(line.RDPID, line.lineID, returnablePoints);
			result_producer.send(session.createObjectMessage( result));
			System.out.printf("%d:%d:%d result\n", id, line.RDPID, line.lineID);
		}
	}

	public RDPresult RDP(TaskOrderedLine<?> line, double epsilon) throws InterruptedException {
		TaskSearch task = new TaskSearch(line, settings.COMPUTATION_SEARCH_PARTS);
		SearchResult result = task.doSearch();
		if (result.furthestDistance > epsilon) {
			return line.asSplit(result.furthestIndex);
		} else {
			return line.asResult();
		}
//		if (line.points.size() < settings.COMPUTATION_THRESHOLD_SIZESEARCH) {
//			try {
//				SearchResult result = FindMostDistant.searchTasks(line, executor, settings.COMPUTATION_SEARCHERS);
//				furthestDistance = result.furthestDistance;
//				furthestIndex = result.furthestIndex;
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//		} else {
//
//			for (int i = 0; i < line.points.size(); i++) {
//				double distance = line.distance(line.points.get(i));
//				if (distance > furthestDistance) {
//					furthestDistance = distance;
//					furthestIndex = i;
//				}
//			}
//		}
		
	}

	private static Comparator<TaskOrderedLine<?>> biggestFirst = new Comparator<TaskOrderedLine<?>>() {

		@Override
		public int compare(TaskOrderedLine<?> o1, TaskOrderedLine<?> o2) {
			return o1.points.size() - o2.points.size();
		}
	};

	private boolean stop = false;
	@Override
	public void stop() {
		stop = true;
	}

	@Override
	public void run() {
		while (!stop) {
			try {
				process();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		try {
			session.close();			
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("stopping ActiveMQ line consumer");
	}

	

}
