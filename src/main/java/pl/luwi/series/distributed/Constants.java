package pl.luwi.series.distributed;

public class Constants {
	public static final String ACTIVEMQ_URL = "failover:(tcp://192.168.1.29:61616,localhost:8161)";
	public static final String ACTIVEMQ_USER = "admin";
	public static final String ACTIVEMQ_PASSWORD = "admin";

	public static final String REGISTRATION_MASTER = "ATHENA";
	public static final int REGISTRATION_PORT = 1200;
	public static final String REGISTRATION_NAME = "ProcessRegistration";

	public static final String QUEUE_REGISTRAR = "1_LINE_REGISTER";
	public static final String QUEUE_FINDMAX_SPREADER = "2_FINDMAX_SPREADER";
	public static final String QUEUE_FINDMAX_PROCESSOR = "3_FINDMAX_PROCESSOR";
	public static final String QUEUE_FINDMAX_GATHER = "4_FINDMAX_GATHER";
	public static final String QUEUE_SPLITTER = "5_LINE_SPLITTER";
	public static final String QUEUE_RESULT_GATHER = "6_LINE_RESULT_GATHER";
	public static final String QUEUE_PICKUP = "7_LINE_PICKUP";

	public static final double QUEUE_FINDMAX_SPREADER_LENGTHTHRESHOLD = 5000.0;
	public static final int QUEUE_FINDMAX_SPREADER_SEGMENTS = 5;
	public static final double SPLIT_EPSILON = 20;

}
