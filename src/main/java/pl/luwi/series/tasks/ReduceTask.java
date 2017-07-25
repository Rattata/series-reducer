package pl.luwi.series.tasks;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import pl.luwi.series.reducer.OrderedPoint;
import pl.luwi.series.reducer.Point;
import pl.luwi.series.reducer.PointSegment;

public class ReduceTask<P extends Point> implements Callable<Void>,Prioritized {

	ExecutorService executor;
	PointSegment<P> segment;
	public ReduceResult<P> result;
	double epsilon;

	public ReduceTask(PointSegment<P> points, ExecutorService service, ReduceResult<P> result, double epsilon) {
		super();
		executor = service;
		this.result = result;
		segment = points;
		this.epsilon = epsilon;
	}

	public ReduceTask(List<P> points, ExecutorService service, double epsilon) {
		this(new PointSegment<>(points), service, new ReduceResult<P>(), epsilon);
	}
	
	@Override
	public int getPriority() {
		return segment.points.size();
	}

	@Override
	public Void call() throws Exception {
		try {
			FindMaximumTask<P> findMaximum = new FindMaximumTask<>(segment, 0, segment.points.size() - 1);
			executor.submit(findMaximum).get();
			
			if(findMaximum.bestDistance > epsilon) {
				List<ReduceTask<P>> subtasks  = new ArrayList<>();
				for(PointSegment<P> newsegment : segment.split(segment, findMaximum.best.getIndex())){
					ReduceTask<P> subtask = new ReduceTask<P>(newsegment, executor, result, epsilon);
					subtasks.add(subtask);
				}
				executor.invokeAll(subtasks);
				System.out.println(result);
				
			} else {
				for (OrderedPoint<P> orderedPoint : segment.asList()) {
					result.newpoints.putIfAbsent(orderedPoint.getIndex(), orderedPoint.getPoint());					
				}
			}
			return null;

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
