package pl.luwi.series.distributed;

import static pl.luwi.series.distributed.Constants.*;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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

public class ProcessingRegistrar extends UnicastRemoteObject implements RegistrationService {

	Session session;
	MessageProducer producer;
	MessageConsumer consumer;
	
	

	public static void main(String[] args) throws Exception {
		ProcessingRegistrar registrar = new ProcessingRegistrar();
		Registry registry;
		// start the registry service on this host
		try {
			registry = LocateRegistry.createRegistry(REGISTRATION_PORT);
		} catch (Exception e) {
			System.out.println("Registry was running already");
			registry = LocateRegistry.getRegistry(REGISTRATION_PORT);
		}

		RegistrationService registrationService = null;
		try {
			registrationService = (RegistrationService) registry.lookup(REGISTRATION_NAME);
		} catch (Exception e) {
			System.out.println("Registration service is not running");
			System.out.println("Starting up Registration Service");
			registry.bind(REGISTRATION_NAME, registrar);
			registrationService = (RegistrationService) registry.lookup(REGISTRATION_NAME);
		}
		while(true){
			registrar.process();
		}
	}

	public ProcessingRegistrar() throws JMSException, RemoteException {
		ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(ACTIVEMQ_USER, ACTIVEMQ_PASSWORD,
				ACTIVEMQ_URL);

		Connection connection = connectionFactory.createConnection();
		connection.start();
		session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		// Destination destination_fromQueue = session.createQueue(QUEUE_LINES);
		// consumer = session.createConsumer(destination_fromQueue);

		Destination destination_toLineQueue = session.createQueue(QUEUE_LINES);
		Destination destination_fromResultQueue = session.createQueue(QUEUE_RESULTS);
		producer = session.createProducer(destination_toLineQueue);
		consumer = session.createConsumer(destination_fromResultQueue);
	}

	public static RegistrationService connect() {
		RegistrationService registrationService = null;
		try {
			Registry r = LocateRegistry.getRegistry(REGISTRATION_MASTER, REGISTRATION_PORT);
			Remote obj = r.lookup(REGISTRATION_NAME);
			System.out.println(obj);
			registrationService = (RegistrationService) obj;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return registrationService;
	}

	HashMap<Integer, CalculationContainer<?>> calculations = new HashMap<>();
	int calculationID = 0;

	@Override
	public <P extends OrderedPoint> List<P> reduce(List<P> points, double epsilon)
			throws InterruptedException, JMSException {
		if (epsilon <= 0) {
			throw new IllegalArgumentException("Epsilon cannot be less then 0.");
		}
		int ID = calculationID++;
		System.out.printf("received: assigned %d to remove points within %.2f of line with N:%d\n", ID, epsilon, points.size());

		TaskOrderedLine<P> message = new TaskOrderedLine<>(points, ID, getLineID());
		CalculationContainer<P> container = new CalculationContainer<>(ID, epsilon, message);
		calculations.put(ID, container);
		producer.send(session.createObjectMessage(message));

		// await final result
		container.latch.await();

		// register calculation
		// create latches
		List<P> returnpoints = (List<P>) container.results();
		System.out.printf("return: %d\n", ID );
		calculations.remove(ID);
		return returnpoints;
	}

	int lineIDs = 0;
	
	public void process() throws Exception{
		Message message =consumer.receive();
		if(message instanceof ObjectMessage){
			LineResult result = (LineResult)((ObjectMessage)message).getObject();
			submitResult(result.RPDID, result.lineID, result.results);
		}
	}
	
	@Override
	public synchronized List<Integer> getLineIDs() {
		lineIDs += REGISTRATION_ID_BATCHSIZE;
		List<Integer> validIDs = IntStream.rangeClosed(lineIDs, lineIDs + REGISTRATION_ID_BATCHSIZE).boxed()
				.collect(Collectors.toList());
		return validIDs;
	}

	public synchronized int getLineID() {
		return lineIDs++;
	}

	@Override
	public Double getEpsilon(Integer calculationID) {
		return calculations.get(calculationID).Epsilon;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <P extends OrderedPoint> void submitResult(int RDPid, int lineID, List<P> result) {
		CalculationContainer<P> container = (CalculationContainer<P>) calculations.get(RDPid);
		boolean done = container.appendResult(lineID, result);
		if (done) {
			container.latch.countDown();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <P extends OrderedPoint> void submitExpectation(int RDPid, int lineID) {
		CalculationContainer<P> container = (CalculationContainer<P>) calculations.get(RDPid);
		container.signalResult(lineID);
	}
}
