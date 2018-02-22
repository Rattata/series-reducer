package pl.luwi.series.distributed;

import static pl.luwi.series.distributed.Constants.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
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

public class ProcessFindmaxGather {

	MessageProducer producer;
	MessageConsumer consumer;
	Session session;

	HashMap<Integer, HashMap<Integer, List<TaskFindMax>>> calculationmap;
	
	public static void main(String[] args) {
		try {
			ProcessFindmaxGather gatherer = new ProcessFindmaxGather();
			gatherer.Process();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void StoreMessage(TaskFindMax task) throws JMSException{
		

		HashMap<Integer, List<TaskFindMax>> result = calculationmap.get(task.calculationIdentifier);
		if(result == null){
			result = new HashMap<Integer, List<TaskFindMax>>();
			calculationmap.put(task.calculationIdentifier, result);
		}
		
		List<TaskFindMax> findMaxResults = result.get(task.spreadID);
		if (findMaxResults == null) {
			findMaxResults = new ArrayList<>();
			result.put(task.spreadID, findMaxResults);
		}
		
		
		findMaxResults.add(task);
		System.out.printf("%d:%d:%d %d/%d%n",task.calculationIdentifier, task.lineID, task.spreadID, findMaxResults.size() , task.totalSegments );
		if(findMaxResults.size() >= task.totalSegments){
			List<OrderedPoint> pointList = findMaxResults.stream().sorted(OrderComparator).flatMap(x -> Arrays.stream(x.points)).collect(Collectors.toList());
			OrderedPoint[] points = new OrderedPoint[pointList.size()];
			pointList.toArray(points);
			
			TaskFindMax masxResult =  findMaxResults.stream().max(FindMaxComparator).get();
			Line line = new Line(masxResult.calculationIdentifier, points, masxResult.start, masxResult.end,  masxResult.lineID);
			TaskSplitLine newTask = new TaskSplitLine(line ,masxResult.furthestDistance, masxResult.maximumIndex);
			
			producer.send(session.createObjectMessage(newTask));
			
			System.out.printf("%d:%d:%d gathered all parts%n", task.calculationIdentifier, task.lineID, task.spreadID);
			result.remove(task.spreadID);
		}
	}

	public void Process() throws JMSException {
		calculationmap = new HashMap<Integer, HashMap<Integer, List<TaskFindMax>>>();
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
			return o1.segment > o2.segment  ? 1 : -1;
		}

	};

	private Comparator<TaskFindMax> FindMaxComparator = new Comparator<TaskFindMax>() {

		@Override
		public int compare(TaskFindMax o1, TaskFindMax o2) {
			return o1.furthestDistance >= o2.furthestDistance ? 1 : -1;
		}

	};
	
	public ProcessFindmaxGather() throws JMSException {
		ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(ACTIVEMQ_USER, ACTIVEMQ_PASSWORD,
				ACTIVEMQ_URL);

		Connection connection = connectionFactory.createConnection();
		connection.start();
		session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		Destination destination_fromQueue = session.createQueue(QUEUE_FINDMAX_GATHER);
		consumer = session.createConsumer(destination_fromQueue);
		Destination destination_toQueue = session.createQueue(QUEUE_SPLITTER);
		producer = session.createProducer(destination_toQueue);

	}

}
