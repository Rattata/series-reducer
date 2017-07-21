package pl.luwi.series.reducer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import pl.luwi.series.tasks.ConcurrentFindMaximumTask;
import pl.luwi.series.tasks.FindMaximumTask;
import pl.luwi.series.tasks.PrioritizedTask;

public class ConcurrentSeriesReducer {
	public static final int coreCount = Runtime.getRuntime().availableProcessors();
	 /**
     * Reduces number of points in given series using Ramer-Douglas-Peucker algorithm.
     * 
     * @param points
     *          initial, ordered list of points (objects implementing the {@link Point} interface)
     * @param epsilon
     *          allowed margin of the resulting curve, has to be > 0
     */
    public static <P extends Point> List<P> reduce(List<P> points, double epsilon) {
        if (epsilon < 0) {
            throw new IllegalArgumentException("Epsilon cannot be less then 0.");
        }
        
        PointSegment<P> rootSegment = new PointSegment<P>(points);
        
        PriorityBlockingQueue<Runnable> work = new PriorityBlockingQueue<Runnable>();
        
        ThreadPoolExecutor executor = new ThreadPoolExecutor(coreCount, -1, 20000, TimeUnit.DAYS, work);
        
        executor.prestartAllCoreThreads();
        
        return executor.submit(newTask).ge;
        
        
    }
}
