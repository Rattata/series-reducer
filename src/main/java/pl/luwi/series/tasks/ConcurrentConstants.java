package pl.luwi.series.tasks;

public class ConcurrentConstants {
	public static final int CONCURRENCY_THRESHOLD = 12500;
	public static final int CORES = Runtime.getRuntime().availableProcessors();
	public static final int TIMEOUT_uS = 900;
	public static final int FINDMAX_CHUNK_SIZE = 12500; 
}
