package pl.luwi.series.tasks;

import java.util.concurrent.Callable;
import java.util.concurrent.RunnableFuture;

import pl.luwi.series.reducer.OrderedPoint;

public interface PrioritizedTask<V> extends Callable<V>, Comparable<PrioritizedTask<?>>{
	
	public int getPriority();
	
}
