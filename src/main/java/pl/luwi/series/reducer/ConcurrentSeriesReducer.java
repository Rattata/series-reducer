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
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.security.auth.x500.X500Principal;

import com.fasterxml.jackson.databind.ObjectWriter.GeneratorSettings;

import static pl.luwi.series.tasks.ConcurrentConstants.*;

import pl.luwi.series.tasks.FindMaximumSpreadGather;
import pl.luwi.series.tasks.Process;
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
		
		LinkedBlockingQueue<PointSegment<P>> segments = new LinkedBlockingQueue<>();
		LinkedBlockingQueue<OrderedPoint<P>> results = new LinkedBlockingQueue<>();
		
		segments.add(new PointSegment<>(points));
		
		Process root = new Process() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				
			}
			
		};
		
		ReduceTask<P> reduce;
		FindMaximumSpreadGather<P> findmaxGatherSpreader;
		reduce = new ReduceTask<>(epsilon, root);
		findmaxGatherSpreader = new FindMaximumSpreadGather<>(2, reduce, reduce.outputQueue, reduce.inputQueue);
		reduce.outputQueue = segments;
		reduce.inputQueue = new LinkedBlockingQueue<>();
		reduce.resultQueue = results;
		
		
		
		Thread reduceTrhead = new Thread(reduce);
		Thread fidmaxer = new Thread(findmaxGatherSpreader);
		
		reduceTrhead.start();
		fidmaxer.start();
		
//		while(!future.isDone()) {
//			System.out.println("\n\n\n");
//			System.out.println("Reduce inputqueue: " + reduce.inputQueue.size());
//			System.out.println("Reduce outqueue: " + reduce.outputQueue.size());
//			System.out.println("Reduce resultqueue: " + reduce.resultQueue.size());
//			System.out.println("Findgather outqueue: " + findmaxGatherSpreader.outputQueue.size());
//			System.out.println("Findgather inqueue: " + findmaxGatherSpreader.inputQueue.size());
//		}
		reduceTrhead.join();
		fidmaxer.join();
		List<P> returnPoints = null; 
//				filteredPoints.entrySet().stream().sorted(new Comparator<Entry<Integer, P>>() {
//			@Override
//			public int compare(Entry<Integer, P> arg0, Entry<Integer, P> arg1) {
//				return Integer.compare(arg0.getKey(), arg1.getKey());
//			}
//		}).map(x -> x.getValue()).collect(Collectors.toList());

		for (P p : returnPoints) {
			System.out.println(p.toString());
		}
		return returnPoints;

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
