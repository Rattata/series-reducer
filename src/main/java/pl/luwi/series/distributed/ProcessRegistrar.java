package pl.luwi.series.distributed;

import static pl.luwi.series.distributed.Constants.*;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

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
import org.apache.activemq.network.ConditionalNetworkBridgeFilterFactory;

import static pl.luwi.series.distributed.Constants.*;
import pl.luwi.series.reducer.Point;

public class ProcessRegistrar extends UnicastRemoteObject implements RegistrationService {

	MessageProducer producer;
	MessageProducer resultproducer;
	MessageConsumer consumer;
	Session session;

	public static void main(String[] args) {
		try {
			ProcessRegistrar registrar = new ProcessRegistrar();

			Registry registry;
			// start the registry service on this host
			try {
				registry = LocateRegistry.createRegistry(REGISTRATION_PORT);
			} catch (Exception e) {
				System.out.println("Registry was running already");
				// get the registry service that is running already on this node
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

			try {
				registrar.Process();
			} catch (Exception e) {
				e.printStackTrace();
			}

		} catch (Exception e) {
			System.err.println(e);
		}
	}

	public void Process() throws JMSException {
		while (true) {
			try {
				Message message = consumer.receive();
				if (message instanceof ObjectMessage) {
					Line task = (Line) ((ObjectMessage) message).getObject();
					if (task.calculationIdentifier == null) {
						task.calculationIdentifier = getCalculationID();
						System.out.println("registered calculation: " + task.calculationIdentifier);
					}
					TaskFindmaxSpread spread = new TaskFindmaxSpread(task);
					producer.send(session.createObjectMessage(spread));
					resultproducer.send(session
							.createObjectMessage(new SignalResultExpectation(task.calculationIdentifier, task.lineID)));

				} else {
					System.err.println("message could not be deserialized");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};

	public ProcessRegistrar() throws JMSException, RemoteException {
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

	int spreadID = 0;

	@Override
	public List<Integer> getSpreadIDs(int number) throws RemoteException {
		List<Integer> spreadIDs = new ArrayList<>();
		for (int i = 0; i < number; i++) {
			spreadIDs.add(spreadID++);
		}
		return spreadIDs;
	}

	int lineID = 0;

	@Override
	public List<Integer> getLineIDs(int number) throws RemoteException {
		List<Integer> lineIDS = new ArrayList<>();
		for (int i = 0; i < number; i++) {
			lineIDS.add(lineID++);
		}
		return lineIDS;
	}

	int calculationID = 0;

	@Override
	public int getCalculationID() throws RemoteException {
		return calculationID++;
	}
	
	HashMap<Integer,CountDownLatch> latches = new HashMap<>();
	HashMap<Integer, List<Integer>> results = new HashMap<>();
	@Override
	public <P extends Point> List<P> reduce(List<P> points, double epsilon) throws RemoteException {
		try {
			OrderedPoint[] orderedPoints = new OrderedPoint[points.size()];
			for (int i = 0; i < points.size(); i++) {
				orderedPoints[i] = new OrderedPoint(points.get(i), i);
			}
			Line task = new Line(new Random().nextInt(), orderedPoints, 0);
			if (task.calculationIdentifier == null) {
				task.calculationIdentifier = getCalculationID();
				System.out.println("registered calculation: " + task.calculationIdentifier);
			}
			TaskFindmaxSpread spread = new TaskFindmaxSpread(task);
			producer.send(session.createObjectMessage(spread));
			resultproducer.send(
					session.createObjectMessage(new SignalResultExpectation(task.calculationIdentifier, task.lineID)));
			CountDownLatch latch = new CountDownLatch(1);
			latches.put(task.calculationIdentifier, latch);
			
			latch.await();
			List<P> retResult = results.get(task.calculationIdentifier).stream().map(x -> points.get(x)).collect(Collectors.toList());
			latches.remove(task.calculationIdentifier);
			results.remove(task.calculationIdentifier);
			return retResult;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static RegistrationService connect() {
		RegistrationService registrationService = null;
		try {
			Registry r = LocateRegistry.getRegistry(REGISTRATION_MASTER, REGISTRATION_PORT);
			registrationService = (RegistrationService) r.lookup(REGISTRATION_NAME);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return registrationService;
	}

	@Override
	public void putResult(List<Integer> indices, Integer calculationID) throws RemoteException {
		results.put(calculationID, indices);
		latches.get(calculationID).countDown();
	}

	
	HashMap<Integer, FindmaxSpreadContainer> spreadResults = new HashMap<>();
	ReentrantLock lock = new ReentrantLock();
	@Override
	public List<TaskFindMax> storeFindMax(TaskFindMax max) throws RemoteException {
		lock.lock();
		FindmaxSpreadContainer container = spreadResults.get(max.calculationIdentifier);
		if(container == null){
			container = new FindmaxSpreadContainer();
			spreadResults.put(max.calculationIdentifier, container);
		}
		List<TaskFindMax> results =  container.storeFindMax(max);
		lock.unlock();
		return results;
	}

}
