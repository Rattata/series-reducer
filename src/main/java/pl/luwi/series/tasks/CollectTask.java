package pl.luwi.series.tasks;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import pl.luwi.series.reducer.Point;

public class CollectTask<P extends Point> implements PrioritizedTask<CollectResult> {
	
	public Future<SplitResult<P>> future; 
	
	@Override
	public CollectResult call() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int compareTo(PrioritizedTask<?> o) {
		return Integer.compare(getPriority(), o.getPriority());
	}

	@Override
	public int getPriority() {
		// TODO Auto-generated method stub
		return future.isDone() ? Integer.MAX_VALUE : Integer.MIN_VALUE;
	}
	
	
	
}
