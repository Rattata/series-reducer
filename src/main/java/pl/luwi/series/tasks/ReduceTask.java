package pl.luwi.series.tasks;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import pl.luwi.series.reducer.OrderedPoint;
import pl.luwi.series.reducer.Point;
import pl.luwi.series.reducer.PointSegment;

public class ReduceTask<P extends Point> implements Callable<ReduceResult<P>>,Prioritized {

	ExecutorService executor;
	PointSegment<P> segment;
	public ReduceResult<P> result;
	double epsilon;

	public ReduceTask(PointSegment<P> points, ExecutorService taskpool, double epsilon) {
		super();
		this.executor = taskpool;
		result = new ReduceResult<>();
		segment = points;
		this.epsilon = epsilon;
	}

	public ReduceTask(List<P> points, ExecutorService taskpool, double epsilon) {
		this(new PointSegment<>(points), taskpool, epsilon);
	}
	
	@Override
	public int getPriority() {
		return segment.points.size();
	}

	@Override
	public ReduceResult<P> call() throws Exception {
		try {
			FindMaximumTask<P> findMaximum = new FindMaximumTask<>(segment, 0, segment.points.size() - 1);
			executor.submit(findMaximum).get();
			
			if(findMaximum.bestDistance > epsilon) {
				List<ReduceTask<P>> subtasks  = new ArrayList<>();
				for(PointSegment<P> newsegment : segment.split(findMaximum.bestIndex)){
					result.segments.add(newsegment);
				}
				executor.invokeAll(subtasks);
				
			} else {
				for (OrderedPoint<P> orderedPoint : segment.asList()) {
					result.filteredPoints.add( orderedPoint);					
				}
			}
			return result;

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
