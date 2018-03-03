package pl.luwi.series.reducer;

public class Stopwatch {
	private static long nanos;
	
	public static void Start() {
		nanos = System.nanoTime();
	};
	
	public static long Stop() {
		return System.nanoTime() - nanos;
	};
}
