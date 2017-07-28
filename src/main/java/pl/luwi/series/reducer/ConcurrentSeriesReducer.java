package pl.luwi.series.reducer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
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
import pl.luwi.series.tasks.ReduceResult;
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
	public static <P extends Point> List<P> reduce(List<P> points, double epsilon)
			throws InterruptedException, ExecutionException {
		 ExecutorService exec = Executors.newCachedThreadPool();
//		ExecutorService exec = new ThreadPoolExecutor(2000, 1000000, 1, TimeUnit.DAYS,
//				new PriorityBlockingQueue<Runnable>((int) Math.sqrt(points.size()), new PriorityFutureComparator())) {
//
//			protected <T> RunnableFuture<T> newTaskFor(Callable<T> callable) {
//				RunnableFuture<T> newTaskFor = super.newTaskFor(callable);
//				return new PriorityFuture<T>(newTaskFor, ((Prioritized) callable).getPriority());
//			}
//		};
//		List<PointSegment<P>> segments = new ArrayList<PointSegment<P>>();
		ArrayBlockingQueue<PointSegment<P>> segments = new ArrayBlockingQueue<>(200);
		segments.add(new PointSegment<>(points));
		List<Future<ReduceResult<P>>> futures = new ArrayList<>();
		ConcurrentHashMap<Integer, P> filteredPoints = new ConcurrentHashMap<>();

		while (!(segments.isEmpty() && futures.isEmpty())) {
			System.out.println("putPointss");
			for (Future<ReduceResult<P>> future : futures) {
				if (future.isDone()) {
					segments.addAll(future.get().segments);
					for (OrderedPoint<P> point : future.get().filteredPoints) {
						filteredPoints.putIfAbsent(point.getIndex(), point.getPoint());
					}
				}
			}
			PointSegment<P> tobeProcessed = segments.remove();
			ReduceTask<P> task = new ReduceTask<P>(tobeProcessed,exec, epsilon);
			System.out.println(segments.remainingCapacity());
			System.out.println(futures.size());
			futures.add(exec.submit(task));
		}

		List<P> returnPoints = filteredPoints.entrySet().stream().sorted(new Comparator<Entry<Integer, P>>() {
			@Override
			public int compare(Entry<Integer, P> arg0, Entry<Integer, P> arg1) {
				return Integer.compare(arg0.getKey(), arg1.getKey());
			}
		}).map(x -> x.getValue()).collect(Collectors.toList());

		for (P p : returnPoints) {
			System.out.println(p.toString());
		}
		exec.shutdown();
		exec.awaitTermination(1000, TimeUnit.NANOSECONDS);
		return returnPoints;

	}

	public ConcurrentSeriesReducer() {
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) {
		ConcurrentSeriesReducer reducer = new ConcurrentSeriesReducer();

		ArrayList<MyPoint> points = new ArrayList<>();
		for (int i = 0; i < 2000; i++) {
			points.add(new MyPoint());
		}
		try {
			ConcurrentSeriesReducer.reduce(points, 1);
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

}
