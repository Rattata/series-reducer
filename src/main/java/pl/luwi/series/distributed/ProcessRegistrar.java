package pl.luwi.series.distributed;

import static pl.luwi.series.distributed.Constants.*;

import java.util.Random;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import org.apache.activemq.ActiveMQConnectionFactory;

public class ProcessRegistrar {

	MessageProducer producer;
	MessageProducer resultproducer;
	MessageConsumer consumer;
	Session session;


	public static void main(String[] args) {
		try {
			ProcessRegistrar registrar = new ProcessRegistrar();
			while (true) {
				try {
					registrar.Process();
					
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} catch (Exception e) {
			System.err.println(e);
		}

	}

	public void Process() throws JMSException {
		while (true) {
			Message message = consumer.receive();
			if (message instanceof ObjectMessage) {
				Line task = (Line) ((ObjectMessage) message).getObject();
				if (task.calculationIdentifier == null) {
					task.calculationIdentifier = new Random().nextInt();
					System.out.println("registered calculation: " + task.calculationIdentifier);
				}
				TaskFindmaxSpread spread = new TaskFindmaxSpread(task);
				producer.send(session.createObjectMessage(spread));
				resultproducer.send(session.createObjectMessage(new SignalResultExpectation(task.calculationIdentifier, task.lineID)));
				
			} else {
				System.err.println("message could not be deserialized");
			}

		}
	};

	public ProcessRegistrar() throws JMSException {
		ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(ACTIVEMQ_USER, ACTIVEMQ_PASSWORD,
				ACTIVEMQ_URL);

		Connection connection = connectionFactory.createConnection();
		connection.start();
		session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		Destination destination_fromQueue = session.createQueue(QUEUE_REGISTRAR);
		consumer = session.createConsumer(destination_fromQueue);
		Destination destination_toQueue = session.createQueue(QUEUE_FINDMAX_SPREADER);
		producer = session.createProducer(destination_toQueue);
		Destination destination_toResultQueue = session.createQueue(QUEUE_RESULT_GATHER);
		resultproducer = session.createProducer(destination_toResultQueue);

	}

}
