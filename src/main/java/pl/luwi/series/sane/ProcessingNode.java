package pl.luwi.series.sane;

import static pl.luwi.series.sane.Constants.ACTIVEMQ_PASSWORD;
import static pl.luwi.series.sane.Constants.ACTIVEMQ_URL;
import static pl.luwi.series.sane.Constants.ACTIVEMQ_USER;
import static pl.luwi.series.sane.Constants.QUEUE_LINES;
import static pl.luwi.series.sane.Constants.REGISTRATION_MASTER;
import static pl.luwi.series.sane.Constants.REGISTRATION_NAME;
import static pl.luwi.series.sane.Constants.REGISTRATION_PORT;

import java.rmi.AlreadyBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Session;

import org.apache.activemq.ActiveMQConnectionFactory;

public class ProcessingNode extends UnicastRemoteObject implements UpdatableNode{
	
	//lifecycle 1, init, update
	ExecutorService executor;
	List<IStoppable> stoppableConsumers;
	ProcessLineSettings settings;

	//lifecycle 2, destruction
	RegistrationService registrar;
	Connection connection;
	ActiveMQConnectionFactory connectionFactory;
	Session session;
	Destination destination_fromQueue;
	
	
	public static void main(String[] args){
		try {
			ProcessingNode node = new ProcessingNode();
			node.init(new ProcessLineSettings());
			node.start();
			String host = args.length > 0 ? args[0] : "192.168.1.39";
			System.setProperty("java.rmi.server.hostname",host);
			System.out.printf("logical cores: %d \n", Runtime.getRuntime().availableProcessors());
			Registry registry;
			// start the registry service on this host
			try {
				registry = LocateRegistry.createRegistry(REGISTRATION_PORT);
				
			} catch (Exception e) {
				System.out.println("Registry was running already");
				registry = LocateRegistry.getRegistry(REGISTRATION_PORT);
			}
			try{
				registry.bind(Constants.UPDATEABLE_NAME, node);				
			} catch(AlreadyBoundException e){
				registry.rebind(Constants.UPDATEABLE_NAME, node);
			}
			System.out.println(registry);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void init(ProcessLineSettings settings) throws Exception{	
		this.settings = settings;
	};
	
	@Override
	public void start() throws RemoteException,Exception{

		
		
		stoppableConsumers = new ArrayList<>();
				
		executor = Executors.newCachedThreadPool();
		
		System.out.printf("started  %d messageconsumers and %d searchers\n", settings.COMPUTATION_CONSUMERS,
				settings.COMPUTATION_SEARCHERS);
		ConsumerLines.settings = settings;
		for (int i = 0; i < settings.COMPUTATION_CONSUMERS; i++) {
			ConsumerLines line = new ConsumerLines(i, executor, registrar, connection, destination_fromQueue);
			stoppableConsumers.add(line);
			executor.submit(line);
		}
		for(int i = 0; i < settings.COMPUTATION_SEARCHERS; i++){
			ConsumerSearchTask searcher = new ConsumerSearchTask();
			stoppableConsumers.add(searcher);
			executor.submit(searcher);
		}
	}
	
	@Override
	public void stop() throws RemoteException, Exception{
		for (IStoppable processLine : stoppableConsumers) {
			processLine.stop();
		}
		stoppableConsumers = null;
		executor.shutdown();
		executor.shutdownNow();
		System.out.println("executor stopped: " + executor.awaitTermination(1, TimeUnit.SECONDS));
	}
	
	
	public static UpdatableNode connect(String host) {
		UpdatableNode registrationService = null;
		try {
			Registry r = LocateRegistry.getRegistry(host, REGISTRATION_PORT);
			Remote obj = r.lookup(Constants.UPDATEABLE_NAME);
			System.out.println(obj);
			registrationService = (UpdatableNode) obj;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return registrationService;
	}

	
	public ProcessingNode() throws RemoteException, Exception {
		this.connectionFactory = new ActiveMQConnectionFactory(ACTIVEMQ_USER, ACTIVEMQ_PASSWORD,
				ACTIVEMQ_URL);
		this.connectionFactory.setUseAsyncSend(true);
		this.connectionFactory.setAlwaysSessionAsync(true);
		this.connection = connectionFactory.createConnection();
		this.registrar = ProcessRegistrar.connect();
		connection.start();
		session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		destination_fromQueue = session.createQueue(QUEUE_LINES);
	}

	@Override
	public void update(ProcessLineSettings update) throws RemoteException {
		try {
			System.out.printf("received settings update %d, restarting workers\n", update.id);
			stop();
			init(update);
			start();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

}
