package pl.luwi.series.tasks;

import java.util.Stack;
import java.util.concurrent.Callable;

import pl.luwi.series.reducer.Point;
import pl.luwi.series.reducer.PointSegment;

public class SplitTask<P extends Point> implements Callable<SplitResult<P>> {
	PointSegment<P> segment;
	double epsilon;
	
	public SplitTask(PointSegment<P> segment, double epsilon) {
		this.segment = segment;
		this.epsilon = epsilon;
	}
	
	public SplitResult<P> call() throws Exception {
		if (epsilon < 0) {
			throw new IllegalArgumentException("Epsilon cannot be less then 0.");
		}
		Stack<PointSegment<P>> work = new Stack<>();
		SplitResult<P> result = new SplitResult<>();
		work.push(segment);
		while (!work.isEmpty()) {
			FindMaximumTask<P> findmaximum = new FindMaximumTask<>(segment);
			FindMaximumResult<P> findresult = findmaximum.call();
			
			if (findresult.furthestDistance > epsilon) {
				for (PointSegment<P> newsegment : segment.split(segment, findresult.result.getIndex())) {
					work.push(newsegment);
				}
			} else {
				result.newpoints.addAll(segment.asList());
			}
		}
		return result;
	};
}
