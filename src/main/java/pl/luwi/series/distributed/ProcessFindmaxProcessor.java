package pl.luwi.series.distributed;

import static pl.luwi.series.distributed.Constants.*;

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

public class ProcessFindmaxProcessor {

	MessageProducer producer;
	MessageConsumer consumer;
	Session session;
	public static void main(String[] args) {
		try {
			ProcessFindmaxProcessor spreader = new ProcessFindmaxProcessor();
			spreader.Process();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void Process() throws JMSException {
		while (true) {
			Message message = consumer.receive();
			if(message instanceof ObjectMessage){
				TaskFindMax task = (TaskFindMax)((ObjectMessage)message).getObject();
				task.execute();
				producer.send(session.createObjectMessage(task));
			} else {
				System.err.println("message could not be deserialized");
			}
			
		}
	};

	public ProcessFindmaxProcessor() throws JMSException {
		ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(ACTIVEMQ_USER, ACTIVEMQ_PASSWORD,
				ACTIVEMQ_URL);

		Connection connection = connectionFactory.createConnection();
		connection.start();
		session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		Destination destination_fromQueue = session.createQueue(QUEUE_FINDMAX_PROCESSOR);
		consumer = session.createConsumer(destination_fromQueue);
		Destination destination_toQueue = session.createQueue(QUEUE_FINDMAX_GATHER);
		producer = session.createProducer(destination_toQueue);

	}

}
