package pl.luwi.series.reducer;

import java.util.ArrayList;
import java.util.List;

import javax.jms.ConnectionFactory;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Session;
import javax.jms.MessageConsumer;
import javax.jms.Message;
import javax.jms.TextMessage;
import javax.jms.ObjectMessage;
import javax.jms.MessageProducer;


import org.apache.activemq.ActiveMQConnectionFactory;

import pl.luwi.series.tasks.FindMaximum;

public class DistributedSeriesReducer {
	public static String url = "failover:(tcp://169.254.1.1:61616,localhost:8161)";
	private static String subjectFrom = "testQueue1";
    private static String subjectTo = "testQueue2";
    
	public static <P extends Point> List<P> reduce(List<P> points, double epsilon, int findmaxProcesses){
		try {
			ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(url);
			Connection connection = connectionFactory.createConnection();
			connection.start();
			Session session = connection.createSession(true, Session.AUTO_ACKNOWLEDGE);
			
			Destination destination = session.createQueue(subjectFrom);
			MessageConsumer consumer = session.createConsumer(destination);
			Message message = consumer.receive();
			if (message instanceof TextMessage) {
				TextMessage textMessage = (TextMessage) message;
				System.out.println("Received'"+textMessage.getText() + "'");
			}
			if(message instanceof ObjectMessage){
				ObjectMessage textMessage = (ObjectMessage) message;
				FindMaximum max = (FindMaximum) textMessage.getObject();
				
			}
			connection.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new ArrayList<>();
	}
	
	public static void Main(String[] args){
		System.out.println("sysout");
	}
	
}
