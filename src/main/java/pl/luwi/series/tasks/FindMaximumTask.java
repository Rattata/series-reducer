package pl.luwi.series.tasks;

import java.util.concurrent.Callable;

import pl.luwi.series.reducer.OrderedPoint;
import pl.luwi.series.reducer.Point;
import pl.luwi.series.reducer.PointSegment;

public class FindMaximumTask<P extends Point> implements Callable<FindMaximumResult<P>> {
	
	PointSegment<P> segment;
	FindMaximumResult<P> result;
	int startIndex;
	int endIndex;

	int priority;
	
	
	
	public FindMaximumTask(PointSegment<P> segment, int startIndex, int endIndex) {
		this.segment = segment;
		this.startIndex = startIndex;
		this.endIndex = endIndex;
		priority = endIndex - startIndex;
	}
	
	public FindMaximumTask(PointSegment<P> segment, int startIndex, int endIndex, int priority) {
		this(segment, startIndex, endIndex);
		this.priority = priority;
	}

	public FindMaximumTask(PointSegment<P> segment) {
		this(segment, 0, segment.points.size() - 1);
	}

	public FindMaximumResult<P> call() throws Exception {
		for (OrderedPoint<P> point : segment.points.subList(startIndex, endIndex + 1)) {
			double dist = segment.distance(point);
			if (result.furthestDistance < dist) {
				result.result = point;
				result.furthestDistance = dist;
			}
		}
		return result;
	}
	
}
