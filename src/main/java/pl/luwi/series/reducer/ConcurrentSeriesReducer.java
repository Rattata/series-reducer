package pl.luwi.series.reducer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

import org.jfree.data.xy.YIntervalDataItem;

import pl.luwi.series.tasks.FindMaximumSpreader;
import pl.luwi.series.tasks.CommunicatingThread;
import pl.luwi.series.tasks.CommunicatingThread.STATE;
import pl.luwi.series.tasks.ConcurrentConstants;
import pl.luwi.series.tasks.FindMaximum;
import pl.luwi.series.tasks.FindMaximumGather;
import pl.luwi.series.tasks.FindMaximumProcessor;
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
	public static <P extends Point> List<P> reduce(List<P> points, double epsilon, int findmaxThreads)
			throws InterruptedException, ExecutionException {
		if(epsilon < 0){
			throw new IllegalArgumentException("Epsilon must be > 0");
		}
		if(findmaxThreads < 1){
			throw new IllegalArgumentException("minimum of 1 find thread");
		}
		//monitoring
		List<CommunicatingThread<?, ?>> threads = new ArrayList<>();
		List<LinkedBlockingQueue<?>> queues = new ArrayList<>();

		ConcurrentHashMap<Integer,OrderedPoint<?>> results = new ConcurrentHashMap<>();
		
		LinkedBlockingQueue<PointSegment> nonmaxedSegments = new LinkedBlockingQueue<>();
		nonmaxedSegments.add(new PointSegment(points));
		queues.add(nonmaxedSegments);
		
		
		LinkedBlockingQueue<PointSegment> maxedSegments = new LinkedBlockingQueue<>();
		queues.add(maxedSegments);
		
		LinkedBlockingQueue<List<FindMaximum>> findMaximumFirst = new LinkedBlockingQueue<>();
		LinkedBlockingQueue<List<FindMaximum>> findMaximumTemp = findMaximumFirst;
		queues.add(findMaximumFirst);
		LinkedBlockingQueue<List<FindMaximum>> findMaximumLast = null;
		List<FindMaximumProcessor> findMaximumProcessors = new ArrayList<>();
		for(int i = 0 ;i < findmaxThreads; i++){
			
			findMaximumLast = new LinkedBlockingQueue<>();
			queues.add(findMaximumLast);
			
			FindMaximumProcessor processor = new FindMaximumProcessor(findMaximumTemp, findMaximumLast, i, threads);
			findMaximumTemp = findMaximumLast;		
			
			findMaximumProcessors.add(processor);
			threads.add(processor);
		}
				
		ReduceTask splitter = new ReduceTask(maxedSegments, nonmaxedSegments, epsilon, results);
		
		threads.add(splitter);
		FindMaximumGather gatherer = new FindMaximumGather(findMaximumLast, maxedSegments);
		threads.add(gatherer);
		FindMaximumSpreader spreader = new FindMaximumSpreader(nonmaxedSegments, findMaximumFirst, gatherer, 5, findMaximumProcessors);
		threads.add(spreader);
		splitter.setPeers(threads);
		for (CommunicatingThread<?, ?> thread : threads) {
			thread.setPeers(threads);
			thread.start();
		}
		 while(queues.stream().anyMatch(x -> ! x.isEmpty()) || threads.stream().anyMatch(x -> x.getCurrentState().equals(STATE.RUNNING))){
			 //spinwait... :(
			 Thread.sleep(0, ConcurrentConstants.TIMEOUT_uS);
			 Thread.yield();
		 }
		 int summedMisses = 0;
		 for (CommunicatingThread<?, ?> thread : threads) {
			summedMisses += thread.getCommMisses();
			System.out.println(thread.getClass().getName() + ": " + thread.getCommMisses());
			thread.join();
		}
		System.out.println("missed" + summedMisses);
		return results.values().parallelStream().sorted().map(x -> (P)x.getPoint()).collect(Collectors.toList());

	}

	public static void main(String[] args) {
		ConcurrentSeriesReducer reducer = new ConcurrentSeriesReducer();

		ArrayList<MyPoint> points = new ArrayList<>();
		
		for (int i = 0; i < 10000; i++) {
			points.add(new MyPoint());
		}
		try {
			for(int i = 0 ;i < 10; i++){
				System.out.println("\n\n\n");
				ConcurrentSeriesReducer.reduce(points, 1, 5);
				Runtime.getRuntime().gc();
				Thread.sleep(10);
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

}
