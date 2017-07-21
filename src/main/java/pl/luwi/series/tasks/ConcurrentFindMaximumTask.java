package pl.luwi.series.tasks;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.Function;

import pl.luwi.series.reducer.OrderedPoint;
import pl.luwi.series.reducer.Point;
import pl.luwi.series.reducer.PointSegment;

public class ConcurrentFindMaximumTask<P extends Point> extends FindMaximumTask<P> implements PrioritizedTask<FindMaximumResult<P>> {

	public final static int CONCURRENCY_THRESHOLD = 12500;
	public final static int coreCount = Runtime.getRuntime().availableProcessors();
	PointSegment<P> segment;
	ExecutorService taskpool;

	public ConcurrentFindMaximumTask(PointSegment<P> segment, ExecutorService taskpool) {
		super(segment);
		this.taskpool = taskpool;
		this.segment = segment;
	}
	
	Function<Future<FindMaximumResult<P>>, FindMaximumResult<P>> futureUnwrapper = (x) -> {
		try {
			return x.get();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		return null;
	};

	

	@Override
	public FindMaximumResult<P> call() throws Exception {
		if(segment.points.size() < CONCURRENCY_THRESHOLD) {
			return super.call();
		}
		double findsegments = Math.ceil(segment.points.size() / coreCount);
		int segmentSize = segment.points.size();

		ArrayList<Future<FindMaximumResult<P>>> findTasks = new ArrayList<Future<FindMaximumResult<P>>>((int) findsegments);

		for (int i = 0; i < findsegments; i++) {
			
			FindMaximumTask<P> task =  new FindMaximumTask<P>(segment, 0 + segmentSize * i,
					segmentSize + segmentSize * (i + 1), getPriority());
			findTasks.add(taskpool.submit(task));
		}
		
		return result;
	}

	
	@Override
	public int getPriority() {
		return segment.points.size();
	}
	
	@Override
	public int compareTo(PrioritizedTask<?> o) {
		return Integer.compare(getPriority(), o.getPriority());
	}

}
