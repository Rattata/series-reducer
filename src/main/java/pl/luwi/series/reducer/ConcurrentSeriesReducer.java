package pl.luwi.series.reducer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.security.auth.x500.X500Principal;

import static pl.luwi.series.tasks.ConcurrentConstants.*;

import pl.luwi.series.tasks.Prioritized;
import pl.luwi.series.tasks.PriorityFuture;
import pl.luwi.series.tasks.ReduceTask;

public class ConcurrentSeriesReducer {

	/**
	 * Reduces number of points in given series using Ramer-Douglas-Peucker
	 * algorithm.
	 * 
	 * @param points
	 *            initial, ordered list of points (objects implementing the
	 *            {@link Point} interface)
	 * @param epsilon
	 *            allowed margin of the resulting curve, has to be > 0
	 * @throws ExecutionException 
	 * @throws InterruptedException 
	 */
	public static <P extends Point> List<P> reduce(List<P> points, double epsilon) throws InterruptedException, ExecutionException {
		ExecutorService exec = new ThreadPoolExecutor(1000000, 1000000, 1, TimeUnit.DAYS,
                new PriorityBlockingQueue<Runnable>((int)Math.sqrt(points.size()), new PriorityFutureComparator())) {

            protected <T> RunnableFuture<T> newTaskFor(Callable<T> callable) {
                RunnableFuture<T> newTaskFor = super.newTaskFor(callable);
                return new PriorityFuture<T>(newTaskFor, ((Prioritized) callable).getPriority());
            }
        };
		ReduceTask<P> task = new ReduceTask<P>(points, exec, epsilon);
		exec.submit(task).get();
		System.out.println("derp");
		
		List<P> result = task.result.newpoints.keySet().stream()
				.sorted().map((x) -> task.result.newpoints.get(x))
				.collect(Collectors.toList());
		System.out.println(result);
		exec.shutdown();
		exec.awaitTermination(1000, TimeUnit.NANOSECONDS);
		return result;

	}
	
	
	public ConcurrentSeriesReducer() {
		// TODO Auto-generated constructor stub
	}
	
	public static void main(String[] args) {
		ConcurrentSeriesReducer reducer = new ConcurrentSeriesReducer();
		
		ArrayList<MyPoint> points = new ArrayList<>();
		for(int i = 0 ; i < 6; i++) {
			points.add(new MyPoint());
		}
		try {
			ConcurrentSeriesReducer.reduce(points, 100);
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

}
