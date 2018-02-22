package pl.luwi.series.distributed;

import static pl.luwi.series.distributed.Constants.*;

import java.util.List;
import java.util.Random;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;

import org.apache.activemq.ActiveMQConnectionFactory;

import pl.luwi.series.reducer.Point;

public class DistributedSeriesReducer {
	static int calcID = 0;
	public static <P extends Point> List<P> reduce(List<P> points, double epsilon) throws JMSException {
		OrderedPoint[] orderedPoints = new OrderedPoint[points.size()];
		for (int i = 0; i < points.size(); i++) {
			orderedPoints[i] = new OrderedPoint(points.get(i), i);
		}
		Line line = new Line(new Random().nextInt(),orderedPoints, 0);
		

		ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(ACTIVEMQ_USER, ACTIVEMQ_PASSWORD,
				ACTIVEMQ_URL);

		Connection connection = connectionFactory.createConnection();
		connection.start();
		Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		Destination destination_toQueue = session.createQueue(QUEUE_REGISTRAR);
		MessageProducer producer = session.createProducer(destination_toQueue);
		producer.send(session.createObjectMessage(line));
		
		//await resultQueue
		
		
		return null;
	}
}
