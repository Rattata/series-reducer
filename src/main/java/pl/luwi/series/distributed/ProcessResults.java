package pl.luwi.series.distributed;

import static pl.luwi.series.distributed.Constants.*;

import java.util.HashMap;

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

public class ProcessResults {

	MessageProducer producer;
	MessageConsumer consumer;
	Session session;

	HashMap<Integer, CalculationResultTask> resultContainers = new HashMap<>(2);

	public static void main(String[] args) {
		try {
			ProcessResults spreader = new ProcessResults();
			spreader.Process();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void Process() throws JMSException {
		while (true) {
			Message message = consumer.receive();
			if (message instanceof ObjectMessage) {
				Object payload = ((ObjectMessage) message).getObject();
				
				if (payload instanceof SignalResultExpectation) {
					SignalResultExpectation expect = (SignalResultExpectation) payload;
					
					if (!resultContainers.containsKey(expect.calculationIdentifier)) {
						resultContainers.put(expect.calculationIdentifier,
								new CalculationResultTask(expect.calculationIdentifier));
					}
					CalculationResultTask container = resultContainers.get(expect.calculationIdentifier);
					container.expectLine(expect.lineID);
					System.out.printf("%d:%d signal%n",container.calculationIdentifier , expect.lineID);

				} else if (payload instanceof TaskResult) {
					TaskResult result = (TaskResult) payload;
					CalculationResultTask container = resultContainers.get(result.calculationIdentifier);
					
					System.out.printf("%d:%d result%n",container.calculationIdentifier,result.lineID);
					container.receiveResult(result);
					
					if(container.isDone()){
						System.out.println("Container is done");
					}
					
				} else {
					System.out.println("did not recognize object!");
				}
				// TaskFindMax task =
				// (TaskFindMax)((ObjectMessage)message).getObject();
				// task.execute();
				// producer.send(session.createObjectMessage(task));
			} else {
				System.err.println("message could not be deserialized");
			}

		}
	};

	public ProcessResults() throws JMSException {
		ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(ACTIVEMQ_USER, ACTIVEMQ_PASSWORD,
				ACTIVEMQ_URL);

		Connection connection = connectionFactory.createConnection();
		connection.start();
		session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		Destination destination_fromQueue = session.createQueue(QUEUE_RESULT_GATHER);
		consumer = session.createConsumer(destination_fromQueue);
		Destination destination_toQueue = session.createQueue(QUEUE_PICKUP);
		producer = session.createProducer(destination_toQueue);

	}

}
