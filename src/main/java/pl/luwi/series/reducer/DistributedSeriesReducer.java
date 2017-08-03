package pl.luwi.series.reducer;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import javax.jms.ConnectionFactory;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Session;
import javax.jms.TemporaryQueue;
import javax.jms.MessageConsumer;
import javax.jms.Message;
import javax.jms.TextMessage;
import javax.jms.MessageProducer;


import org.apache.activemq.ActiveMQConnectionFactory;

public class DistributedSeriesReducer {
	public static String url = "failover:(tcp://169.254.1.1:61616,localhost:8161)";
	private static String subjectFrom = "testQueue1";
    private static String subjectTo = "testQueue2";
    
	public static <P extends Point> List<P> reduce(List<P> points, double epsilon, int findmaxProcesses){
		Connection conn;
		UUID trace = UUID.randomUUID();
		
		try {
			ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(url);
			conn = connectionFactory.createConnection();
			conn.start();
			Session session = conn.createSession(false, 2);
			TemporaryQueue tempqueue = session.createTemporaryQueue();
			
			MessageConsumer consumer = session.createConsumer(tempqueue);
			Message msg =  consumer.receive(1000);
			//do work 
			msg.acknowledge();
			//done
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new ArrayList<>();
	}
	
	static ConcurrentHashMap<UUID,PointSegment> hostedPoints = new  ConcurrentHashMap();
	public static void HostPointSegment(PointSegment hostme, UUID trace) {
		
		
	}
	
	public static PointSegment receive(UUID trace, int start, int end) {
//		return hostedPoints.get(trace).;
		return null;
	}
	
}
