package pl.luwi.series.sane;

public class Constants {
	public static final String ACTIVEMQ_URL = "failover:(tcp://192.168.1.39:61616,localhost:8161)";
	public static final String ACTIVEMQ_USER = "admin";
	public static final String ACTIVEMQ_PASSWORD = "admin";

	public static final String REGISTRATION_MASTER = "192.168.1.39";
	public static final int REGISTRATION_PORT = 1200;
	public static final String REGISTRATION_NAME = "ProcessRegistration2";
	public static final int REGISTRATION_ID_BATCHSIZE = 1000;
	
	public static final String UPDATEABLE_NAME = "UPDATABLE_PROCESSOR"; 
	
	public static final String QUEUE_LINES =  "1_LINES";
	public static final String QUEUE_SEARCH =  "2_SEARCH";
	public static final String QUEUE_RESULTS =  "2_RESULTS";
	
	public static final int COMPUTATION_THRESHOLD_SIZESPLIT = 50000;
	public static final double COMPUTATION_THRESHOLD_SIZESEARCH = 50000.0;
	public static final int COMPUTATION_SEARCHERS = 4;
	public static final int COMPUTATION_CONSUMERS = 2;
	
}
