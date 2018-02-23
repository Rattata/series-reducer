package pl.luwi.series.distributed;

import static pl.luwi.series.distributed.Constants.*;

import java.util.ArrayList;
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

public class ProcessSplitter {

	MessageProducer findmaxproducer;
	MessageProducer resultproducer;
	MessageConsumer consumer;
	Session session;
	RegistrationService service;
	Stack<Integer> lineIDs = new Stack<>();

	public static void main(String[] args) {
		try {
			ProcessSplitter spreader = new ProcessSplitter();
			while (true) {
				spreader.Process();

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	

	public void Process() throws JMSException {
		while (true) {
			try {
				service = ProcessRegistrar.connect();
				Message message = consumer.receive();
				if (message instanceof ObjectMessage) {
					TaskSplitLine task = (TaskSplitLine) ((ObjectMessage) message).getObject();
					if (task.epsilon >= SPLIT_EPSILON) {
						if(lineIDs.size() < 50){
							lineIDs.addAll(service.getLineIDs(1000));
						}
						int lineID = lineIDs.pop();
						
						Line[] lines = task.line.split(task.index, task.line.lineID, lineID);
						for (Line line : lines) {
							findmaxproducer.send(session.createObjectMessage(new TaskFindmaxSpread(line)));
						}

						/// update expected results
						System.out.printf("%d:%d split -> %d %d %n", task.line.calculationIdentifier, task.line.lineID,
								lines[0].lineID, lines[1].lineID);
						resultproducer.send(session.createObjectMessage(
								new SignalResultExpectation(task.line.calculationIdentifier, lineID)));
						lineID++;
					} else {

						System.out.println(task.line.calculationIdentifier + ":" + task.line.lineID + ":result");
						resultproducer.send(session.createObjectMessage(new TaskResult(task.line.calculationIdentifier,
								task.line.lineID, task.line.start, task.line.end)));
					}

				} else {
					System.err.println("message could not be deserialized");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};

	public ProcessSplitter() throws JMSException {
		ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(ACTIVEMQ_USER, ACTIVEMQ_PASSWORD,
				ACTIVEMQ_URL);

		Connection connection = connectionFactory.createConnection();
		connection.start();
		session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		Destination destination_fromQueue = session.createQueue(QUEUE_SPLITTER);
		consumer = session.createConsumer(destination_fromQueue);
		Destination destination_toQueue = session.createQueue(QUEUE_FINDMAX_SPREADER);
		findmaxproducer = session.createProducer(destination_toQueue);
		Destination destination_toGatheQueue = session.createQueue(QUEUE_RESULT_GATHER);
		resultproducer = session.createProducer(destination_toGatheQueue);

	}

}
