package pl.luwi.series.distributed;

import static pl.luwi.series.distributed.Constants.*;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;
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
import javax.jms.TextMessage;
import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;

public class ProcessFindmaxReduceSegments {

	MessageProducer mapLineProducer;
	MessageProducer findmaxproducer;
	MessageConsumer consumer;
	Session session;

	RegistrationService service;

	Stack<Integer> lineIDs = new Stack<>();

	public static void main(String[] args) {
		try {
			ProcessFindmaxReduceSegments gatherer = new ProcessFindmaxReduceSegments();
			gatherer.Process();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void StoreMessage(TaskFindMax task) throws JMSException, RemoteException {

		List<TaskFindMax> findMaxResults;
		if (task.totalSegments != 1) {
			findMaxResults = service.storeFindMax(task);
		} else {
			findMaxResults = new ArrayList<>();
			findMaxResults.add(task);
		}

		System.out.printf("%d:%d:%d received %d/%d%n", task.calculationIdentifier, task.lineID, task.spreadID,
				task.segment, task.totalSegments);
		if (findMaxResults != null) {
			List<OrderedPoint> pointList = findMaxResults.stream().sorted(OrderComparator)
					.flatMap(x -> Arrays.stream(x.points)).collect(Collectors.toList());
			OrderedPoint[] points = new OrderedPoint[pointList.size()];
			pointList.toArray(points);

			TaskFindMax masxResult = findMaxResults.stream().max(FindMaxComparator).get();
			Line line = new Line(masxResult.calculationIdentifier, points, masxResult.start, masxResult.end,
					masxResult.lineID);

			TaskSplitLine newTask = new TaskSplitLine(line, masxResult.furthestDistance, masxResult.maximumIndex);

//			producer.send(session.createObjectMessage(newTask));

			System.out.printf("%d:%d:%d gathered all parts%n", task.calculationIdentifier, task.lineID, task.spreadID);

			if (newTask.epsilon >= SPLIT_EPSILON) {
				if (lineIDs.isEmpty()) {
					lineIDs.addAll(service.getLineIDs(1000));
				}
				int lineID = lineIDs.pop();
				Line[] lines = newTask.line.split(newTask.index, newTask.line.lineID, lineID);
				
				for (Line line2 : lines) {
					findmaxproducer.send(session.createObjectMessage(new TaskFindmaxSpread(line2)));
				}

				/// update expected results
				System.out.printf("%d:%d split -> %d %d %n", newTask.line.calculationIdentifier, newTask.line.lineID,
						lines[0].lineID, lines[1].lineID);
				mapLineProducer.send(session
						.createObjectMessage(new SignalResultExpectation(newTask.line.calculationIdentifier, lineID)));
				lineID++;
			} else {

				System.out.println(newTask.line.calculationIdentifier + ":" + newTask.line.lineID + ":result");
				mapLineProducer.send(session.createObjectMessage(new TaskResult(newTask.line.calculationIdentifier,
						newTask.line.lineID, newTask.line.start, newTask.line.end)));
			}
		}
	}

	public void Process() throws JMSException, RemoteException {
		service = ProcessRegistrar.connect();
		while (true) {
			Message message = consumer.receive();
			if (message instanceof ObjectMessage) {
				StoreMessage((TaskFindMax) ((ObjectMessage) message).getObject());
			} else {
				System.err.println("message could not be deserialized");
			}

		}
	};

	private Comparator<TaskFindMax> OrderComparator = new Comparator<TaskFindMax>() {

		@Override
		public int compare(TaskFindMax o1, TaskFindMax o2) {
			return o1.segment > o2.segment ? 1 : -1;
		}

	};

	private Comparator<TaskFindMax> FindMaxComparator = new Comparator<TaskFindMax>() {

		@Override
		public int compare(TaskFindMax o1, TaskFindMax o2) {
			return o1.furthestDistance >= o2.furthestDistance ? 1 : -1;
		}

	};

	public ProcessFindmaxReduceSegments() throws JMSException {
		ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(ACTIVEMQ_USER, ACTIVEMQ_PASSWORD,
				ACTIVEMQ_URL);

		Connection connection = connectionFactory.createConnection();
		connection.start();
		session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		Destination destination_fromQueue = session.createQueue(QUEUE_FINDMAX_GATHER);
		consumer = session.createConsumer(destination_fromQueue);
		Destination destination_toResultQueue = session.createQueue(QUEUE_RESULT_GATHER);
		mapLineProducer = session.createProducer(destination_toResultQueue);
		Destination destination_tofindMaxQueue = session.createQueue(QUEUE_FINDMAX_SPREADER);
		findmaxproducer = session.createProducer(destination_tofindMaxQueue);

	}

}
