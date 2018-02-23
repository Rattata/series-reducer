package pl.luwi.series.distributed;

import static pl.luwi.series.distributed.Constants.*;

import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

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

public class ProcessFindmaxMapLineToSegments {

	MessageProducer producer;
	MessageConsumer consumer;
	Session session;

	RegistrationService service;
	
	public static void main(String[] args) {
		try {
			ProcessFindmaxMapLineToSegments spreader = new ProcessFindmaxMapLineToSegments();
			while (true) {
				try {
					spreader.Process();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	Stack<Integer> unusedSpreadIDs = new Stack<>();
	public void Process() throws JMSException , RemoteException{
		service = ProcessRegistrar.connect();
		while (true) {
			Message message = consumer.receive();
			if (message instanceof ObjectMessage) {
				TaskFindmaxSpread task = (TaskFindmaxSpread) ((ObjectMessage) message).getObject();
				System.out.println(task.line.calculationIdentifier + ":"+task.line.lineID);
				TaskFindMax[] result = null;
				Line line = task.line;
				if(unusedSpreadIDs.isEmpty()){
					unusedSpreadIDs.addAll(service.getSpreadIDs(1000));
				}
				
				int spreadID = unusedSpreadIDs.pop();
				int segments = (int)Math.ceil(line.points.length / QUEUE_FINDMAX_SPREADER_LENGTHTHRESHOLD);
				result = new TaskFindMax[segments];
				if(segments == 0){
					result = new TaskFindMax[]{new TaskFindMax(line.points, line, line.calculationIdentifier, spreadID, 1, 1)};
				}
				for (int i = 0; i < segments; i++) {
					int endIndex = (int) (i == (segments - 1)  ? line.points.length - 1
							: (i + 1) * QUEUE_FINDMAX_SPREADER_LENGTHTHRESHOLD);
					OrderedPoint[] subset = Arrays.copyOfRange(line.points, (int)(i * QUEUE_FINDMAX_SPREADER_LENGTHTHRESHOLD),
							endIndex);
					result[i] = new TaskFindMax(subset, line, line.calculationIdentifier, spreadID, segments, i);
				}
				
				for (TaskFindMax taskFindMax : result) {
					System.out.println(taskFindMax.calculationIdentifier + ":" + taskFindMax.lineID + " : " + taskFindMax.spreadID + " : " + taskFindMax.segment + "/" + taskFindMax.totalSegments);
					producer.send(session.createObjectMessage(taskFindMax));
				}
			} else {
				System.err.println("message could not be deserialized");
			}

		}
	};

	public ProcessFindmaxMapLineToSegments() throws JMSException {
		ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(ACTIVEMQ_USER, ACTIVEMQ_PASSWORD,
				ACTIVEMQ_URL);

		Connection connection = connectionFactory.createConnection();
		connection.start();
		session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		Destination destination_fromQueue = session.createQueue(QUEUE_FINDMAX_SPREADER);
		consumer = session.createConsumer(destination_fromQueue);
		Destination destination_toQueue = session.createQueue(QUEUE_FINDMAX_PROCESSOR);
		producer = session.createProducer(destination_toQueue);

	}

}
