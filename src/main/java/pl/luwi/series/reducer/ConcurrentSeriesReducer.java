package pl.luwi.series.reducer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class ConcurrentSeriesReducer {

	private static class Work<P extends Point> {
		Line<P> line;
		List<P> points;

		public Work(Line<P> line, List<P> points) {
			this.line = line;
			this.points = points;
		}
	}
	
	/**
	 * What N and above makes single-core vs multicore vs distributed more profitable?
	 * ["finding largest distance", "splitting array", "reassemble array"] 
	 */
//	amdahl
	private static final int find_multicore = 12;
	private static final int split_multicore = 20;
	private static final int reassemble_multicore = 20;

//	gustafson
	private static final int find_distributed = 12;
	private static final int split_distributed = 20;
	private static final int reassemble_distributed = 20;

	
	private static final int timeout_ns_multicore = 1500;
	private static final int timeout_ms_distributed = 1500;
	
	
	
	/**
	 * Reduces number of points in given series using Ramer-Douglas-Peucker
	 * algorithm.
	 * 
	 * @param points
	 *            initial, ordered list of points (objects implementing the
	 *            {@link Point} interface)
	 * @param epsilon
	 *            allowed margin of the resulting curve, has to be > 0
	 */
	public static <P extends Point> List<P> reduce(List<P> points, double epsilon) {
		if (epsilon < 0) {
			throw new IllegalArgumentException("Epsilon cannot be less then 0.");
		}
		Stack<OrderedLineSegment> work = new Stack<OrderedLineSegment>();
		work.push(new OrderedLineSegment(points));

		OrderedPoint[] result = new OrderedPoint[points.size()];
		
		ExecutorService executor = Executors.newFixedThreadPool(3);
		PriorityBlockingQueue<OrderedLineSegment> workQueue = new PriorityBlockingQueue<>(points.size() / 16);
		
		Callable<Void> task = () -> {return null;};
		
		Future<Void> future = executor.submit(() -> {
			try {
				OrderedLineSegment segment  = workQueue.poll(timeout_ns_multicore, TimeUnit.NANOSECONDS);
				TimeUnit.SECONDS.sleep(2);
				return null;
			} catch (InterruptedException e) {
				System.out.println("err");
			}
			return null;
		});
	
		while (!work.isEmpty()) {
			OrderedLineSegment segment = work.pop();

			double furthestPointDistance = 0.0;
			int furthestPointIndex = 0;
			for (int i = 0; i < segment.getPoints().size() - 1; i++) {
				double distance = segment.distance(segment.getPoints().get(i));
				if (distance > furthestPointDistance) {
					furthestPointDistance = distance;
					furthestPointIndex = i;
				}
			}

			if (furthestPointDistance > epsilon) {
				List<OrderedPoint> firstPoints = segment.getPoints().subList(0, furthestPointIndex + 1);
				OrderedLineSegment firstSegment = new OrderedLineSegment(firstPoints, segment.getStart(),
						segment.getPoints().get(furthestPointIndex));

				List<OrderedPoint> secondPoints = segment.getPoints().subList(furthestPointIndex,
						segment.getPoints().size());
				OrderedLineSegment secondSegment = new OrderedLineSegment(secondPoints,
						segment.getPoints().get(furthestPointIndex), segment.getEnd());

				work.push(firstSegment);
				work.push(secondSegment);
			} else {
				OrderedPoint start = segment.getStart();
				OrderedPoint end = segment.getEnd();
				if (result[end.GetIndex()] == null) {
					result[end.GetIndex()] = end;
				}

				if (result[start.GetIndex()] == null) {
					result[start.GetIndex()] = start;
				}
			}
		}
		ConcurrentHashMap<Integer, OrderedPoint> resultmap = new ConcurrentHashMap<>();
		// collect points
		List<P> reducedPoints = new ArrayList<P>();
		for (int i = 0; i < result.length; i++) {
			if (result[i] != null) {
				reducedPoints.add(points.get(result[i].GetIndex()));
			}
		}
		return reducedPoints;
	}
	
	private class solveResult{
		public OrderedLineSegment[] newsegments;
		public OrderedPoint[] points;
		
		public solveResult(OrderedLineSegment[] newssegments) {
			this.newsegments = newssegments;
		}
		
		public solveResult(OrderedPoint[] newpoints) {
			this.points = newpoints;
		} 
	}
	
	
	private solveResult solve(OrderedLineSegment segment, double epsilon){

		double furthestPointDistance = 0.0;
		int furthestPointIndex = 0;
		for (int i = 0; i < segment.getPoints().size() - 1; i++) {
			double distance = segment.distance(segment.getPoints().get(i));
			if (distance > furthestPointDistance) {
				furthestPointDistance = distance;
				furthestPointIndex = i;
			}
		}
		if (furthestPointDistance > epsilon) {
			List<OrderedPoint> firstPoints = segment.getPoints().subList(0, furthestPointIndex + 1);
			OrderedLineSegment firstSegment = new OrderedLineSegment(firstPoints, segment.getStart(),
					segment.getPoints().get(furthestPointIndex));

			List<OrderedPoint> secondPoints = segment.getPoints().subList(furthestPointIndex,
					segment.getPoints().size());
			OrderedLineSegment secondSegment = new OrderedLineSegment(secondPoints,
					segment.getPoints().get(furthestPointIndex), segment.getEnd());
			solveResult result = new solveResult(new OrderedLineSegment[]{firstSegment, secondSegment});
			return result;
		} else {
			OrderedPoint start = segment.getStart();
			OrderedPoint end = segment.getEnd();
			solveResult result = new solveResult(new OrderedPoint[]{start, end});
			return result;
		}
	}

}
