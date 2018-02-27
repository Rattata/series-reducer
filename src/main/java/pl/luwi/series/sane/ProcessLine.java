package pl.luwi.series.sane;

import static pl.luwi.series.sane.Constants.*;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
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

import static pl.luwi.series.reducer.Stopwatch.*;
import org.apache.activemq.ActiveMQConnectionFactory;

import pl.luwi.series.sane.FindMostDistant.SearchResult;
import pl.luwi.series.sane.OrderedLine.RDPresult;

public class ProcessLine implements Runnable {

	Session session;
	MessageProducer result_producer;
	MessageProducer line_producer;
	MessageConsumer consumer;

	RegistrationService registrar;
	HashMap<Integer, Double> epsilonStore = new HashMap<>();
	Stack<Integer> validLineIDs = new Stack<>();

	ExecutorService executor;

	public static void main(String[] argsv) {
		try {

			int messageConsumers = argsv.length == 0 ? 4 : Integer.valueOf(argsv[0]);
			ExecutorService executor = Executors.newWorkStealingPool();
			for (int i = 0; i < messageConsumers; i++) {
				ProcessLine line = new ProcessLine(executor);
				executor.submit(line);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public ProcessLine(ExecutorService executor) throws JMSException, RemoteException {
		this.executor = executor;
		registrar = ProcessRegistrar.connect();
		ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(ACTIVEMQ_USER, ACTIVEMQ_PASSWORD,
				ACTIVEMQ_URL);

		Connection connection = connectionFactory.createConnection();
		connection.start();
		session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		Destination destination_fromQueue = session.createQueue(QUEUE_LINES);
		consumer = session.createConsumer(destination_fromQueue);

		Destination destination_toResultQueue = session.createQueue(QUEUE_RESULTS);
		result_producer = session.createProducer(destination_toResultQueue);
		line_producer = session.createProducer(destination_fromQueue);

	}

	@Override
	public void run() {
		while (true) {
			try {
				process();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	Stack<OrderedLine<?>> self = new Stack<>();

	public void process() throws JMSException, RemoteException {

		OrderedLine<?> line = null;
		if (!self.isEmpty()) {
			line = self.pop();
		} else {
			Message received = consumer.receive();
			if (!(received instanceof ObjectMessage)) {
				System.err.println(" not an objectmessage!:\n" + received.toString());
			}
			line = (OrderedLine<?>) ((ObjectMessage) received).getObject();
		}

		Double epsilon = epsilonStore.get(line.RDPID);
		if (epsilon == null) {
			epsilon = registrar.getEpsilon(line.RDPID);
			epsilonStore.put(line.RDPID, epsilon);
		}

		if (line.points.size() > COMPUTATION_SIZE_THRESHOLD) {
			RDPresult result = RDP(line, epsilon);
			if (result.points != null) {
				registrar.submitResult(line.RDPID, line.lineID, result.points);
			} else {
				List<OrderedLine<?>> lines = result.lines;
				lines.sort(biggestFirst);
				OrderedLine<?> bigger = lines.get(0);
				OrderedLine<?> smaller = lines.get(1);

				smaller.lineID = line.lineID;
				self.push(smaller);

				if (validLineIDs.empty()) {
					validLineIDs.addAll(registrar.getLineIDs());
				}
				bigger.lineID = validLineIDs.pop();
				registrar.submitExpectation(line.RDPID, bigger.lineID);
				line_producer.send(session.createObjectMessage(bigger));

			}
		} else {
			// solve completely if small
			Stack<OrderedLine<?>> tempstack = new Stack<>();
			List<OrderedPoint> returnablePoints = new ArrayList<>();
			tempstack.push(line);
			while (!tempstack.empty()) {
				OrderedLine<?> templine = tempstack.pop();
				RDPresult result = RDP(templine, epsilon);
				if (result.points != null) {
					for (OrderedPoint orderedPoint : (List<? extends OrderedPoint>) result.points) {
						returnablePoints.add(orderedPoint);
					}
				} else {
					List<OrderedLine<?>> lines = result.lines;
					for (OrderedLine<?> orderedLine : lines) {
						tempstack.push(orderedLine);
					}
				}
			}
			returnablePoints = returnablePoints.stream().distinct().collect(Collectors.toList());
			registrar.submitResult(line.RDPID, line.lineID, returnablePoints);
		}
	}

	public RDPresult RDP(OrderedLine<?> line, double epsilon) {
		double furthestDistance = Double.MIN_VALUE;
		int furthestIndex = -1;

		if (line.points.size() > COMPUTATION_SEARCH_SEGMENT_SIZE) {
			try {
				SearchResult result = FindMostDistant.searchTasks(line, executor);
				furthestDistance = result.furthestDistance;
				furthestIndex = result.furthestIndex;
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} else {

			for (int i = 0; i < line.points.size(); i++) {
				double distance = line.distance(line.points.get(i));
				if (distance > furthestDistance) {
					furthestDistance = distance;
					furthestIndex = i;
				}
			}
		}
		if (furthestDistance > epsilon) {
			return line.asSplit(furthestIndex);
		} else {
			return line.asResult();
		}
	}

	private static Comparator<OrderedLine<?>> biggestFirst = new Comparator<OrderedLine<?>>() {

		@Override
		public int compare(OrderedLine<?> o1, OrderedLine<?> o2) {
			return o1.points.size() - o2.points.size();
		}

	};

}
