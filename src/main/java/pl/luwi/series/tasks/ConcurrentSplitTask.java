package pl.luwi.series.tasks;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

import pl.luwi.series.reducer.Point;
import pl.luwi.series.reducer.PointSegment;

public class ConcurrentSplitTask<P extends Point> extends SplitTask<P>
		implements PrioritizedTask<SplitResult<P>> {
	public static int CONCURRENCY_THRESHOLD = 125000;
	
	ExecutorService taskpool;
	ConcurrentHashMap<Integer, P> collect;

	public ConcurrentSplitTask(PointSegment<P> segment, double epsilon, ExecutorService taskpool,
			ConcurrentHashMap<Integer, P> collect) {
		super(segment, epsilon);
		this.taskpool = taskpool;
		this.collect = collect;
	}

	@Override
	public SplitResult<P> call() throws Exception {
		SplitResult<P> result;
		if (segment.points.size() < CONCURRENCY_THRESHOLD) {
			result = super.call();
		} else {
			result = new SplitResult<>();
		}

		FindMaximumResult<P> findtaskresult = taskpool.submit(new ConcurrentFindMaximumTask<>(segment, taskpool))
				.get();
		if (findtaskresult.furthestDistance > epsilon) {
			for (PointSegment<P> newsegment : segment.split(segment, findtaskresult.result.getIndex())) {
				taskpool.submit(new ConcurrentSplitTask<>(newsegment, epsilon, taskpool, collect));
			}
		} else {
			segment.asList().stream().forEach((x) -> collect.putIfAbsent(x.getIndex(), x.getPoint()));
		}
		return result;
	}

	@Override
	public int compareTo(PrioritizedTask<?> to) {
		return Integer.compare(getPriority(), to.getPriority());
	}

	@Override
	public int getPriority() {
		return segment.points.size();
	}

}
