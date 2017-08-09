package pl.luwi.series.reducer;

import java.util.Enumeration;
import java.util.UUID;

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
import javax.jms.Topic;

import org.apache.activemq.ActiveMQConnectionFactory;

import pl.luwi.series.tasks.FindMaximum;

public class Test1 {

	public static void main(String[] args) {
		UUID sessionID = UUID.randomUUID();
		try {
			
			ConnectionFactory connectionFactory = new ActiveMQConnectionFactory("failover:(tcp://192.168.0.100:61616)");
			Connection connection = connectionFactory.createConnection();
			connection.start();
			Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			
			
			
			
			Topic topic = session.createTopic("announce");
			
			MessageConsumer topicConsumer = session.createConsumer(topic);
			
			
			Thread receiver = null;
			try {				
				receiver = new Thread(new Runnable() {
					
					@Override
					public void run() {
						try {
							Message message = topicConsumer.receive();
							if (message instanceof TextMessage) {
								TextMessage textMessage = (TextMessage) message;
								System.out.println("Received'"+textMessage.getText() + "'");
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});
				receiver.start();
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			MessageProducer topicProducer  =session.createProducer(topic);
			topicProducer.send(session.createTextMessage(sessionID.toString()));
			
			
			Destination destination = session.createQueue("testQueue");
			MessageConsumer consumer = session.createConsumer(destination);
			MessageProducer producer = session.createProducer(destination);
			producer.send(session.createTextMessage(":)"));
			
			Message message = consumer.receive();
			if (message instanceof TextMessage) {
				TextMessage textMessage = (TextMessage) message;
				System.out.println("Received'"+textMessage.getText() + "'");
			}
			if(message instanceof ObjectMessage){
				ObjectMessage objectMessage = (ObjectMessage) message;
				FindMaximum max = (FindMaximum) objectMessage.getObject();
			}
			receiver.join();
			session.close();
			connection.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
