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
import javax.jms.MessageProducer;


import org.apache.activemq.ActiveMQConnectionFactory;

public class DistributedSeriesReducer {
	public static String url = "failover:(tcp://169.254.1.1:61616,localhost:8161)";
	private static String subjectFrom = "testQueue1";
    private static String subjectTo = "testQueue2";
    
	public static <P extends Point> List<P> reduce(List<P> points, double epsilon, int findmaxProcesses){
		Connection conn;
		try {
			ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(url);
			conn = connectionFactory.createConnection();
			conn.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new ArrayList<>();
	}
}
