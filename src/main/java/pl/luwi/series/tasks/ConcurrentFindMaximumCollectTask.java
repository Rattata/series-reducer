package pl.luwi.series.tasks;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.function.Function;

import org.testng.internal.thread.FutureResultAdapter;

import pl.luwi.series.reducer.Point;

public class ConcurrentFindMaximumCollectTask<P extends Point> implements PrioritizedTask<FindMaximumResult<P>>{

	List<Future<FindMaximumResult<P>>> futures; 
	ExecutorService taskpool;
	
	@Override
	public FindMaximumResult<P> call() throws Exception {
		if(futures.stream().anyMatch((x) -> ! x.isDone())) {
			taskpool.submit(this);
			System.err.println("task was started, but futures were not resolved! this might indicate a problem in the priorities");
			return null;
		} else {
			return futures.stream().map(futureUnwrapper).max((x,y) -> x.compareTo(y)).get();
		}
	}
	
	
	public ConcurrentFindMaximumCollectTask(List<Future<FindMaximumResult<P>>> futures, ExecutorService taskpool) {
		 this.taskpool = taskpool;
		 this.futures = futures;
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
	public int compareTo(PrioritizedTask<?> o) {
		return Integer.compare(getPriority(), o.getPriority());
	}

	
	@Override
	public int getPriority() {
		return futures.stream().anyMatch((x) -> ! x.isDone()) ? Integer.MIN_VALUE : Integer.MAX_VALUE;
	}

}
