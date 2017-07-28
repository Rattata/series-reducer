package pl.luwi.series.tasks;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import pl.luwi.series.reducer.OrderedPoint;
import pl.luwi.series.reducer.Point;
import pl.luwi.series.reducer.PointSegment;
import static pl.luwi.series.tasks.ConcurrentConstants.*;

public class FindMaximumTask<P extends Point> implements Callable<Void>,Comparable<FindMaximumTask<P>>,Prioritized {

	ExecutorService service;
	
	PointSegment<P> segment;
	int startIndex;
	int endIndex;
	
	public double bestDistance = Double.MIN_VALUE;
	public int bestIndex = -1;
	
	public FindMaximumTask(ExecutorService service, PointSegment<P> pointsegment, int startIndex, int endIndex) {
		super();
		this.service = service;
		this.startIndex = startIndex;
		this.endIndex = endIndex;
		this.segment = pointsegment;
	}
	
	//no executorservice -> no subtasks
	public FindMaximumTask(PointSegment<P> pointsegment, int startIndex, int endIndex) {
		this(null, pointsegment, startIndex, endIndex);
	}
	
	public int getMax(){
		for(int i = startIndex; i <= endIndex; i++) {
			double dist = segment.distance(segment.points.get(i));
			if(dist > bestDistance) {
				bestDistance = dist;
				bestIndex = i;
			}
		}
		return bestIndex;
	}

	@Override
	public int getPriority() {
		return segment.points.size();
	}

	@Override
	public int compareTo(FindMaximumTask<P> o) {
		return Double.compare(bestDistance, o.bestDistance);
	}

	@Override
	public Void call() throws Exception {
		if(segment.points.size() < CONCURRENCY_THRESHOLD || service == null) {
			getMax();
		} else {
			float delta = segment.points.size() / CORES;
			List<FindMaximumTask<P>> subtasks = new ArrayList<>();
			for(int i = 0 ; i < CORES;i++) {
				int start = (int)Math.round(i * delta - 1);
				int end = (int)Math.round((i+1)* delta);
				subtasks.add(new FindMaximumTask<>(service, segment, start, end));
			}
			try {
				service.invokeAll(subtasks);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			bestIndex = subtasks.stream().max(new Comparator<FindMaximumTask<P>>() {
				public int compare(pl.luwi.series.tasks.FindMaximumTask<P> o1, pl.luwi.series.tasks.FindMaximumTask<P> o2) {
					return o1.compareTo(o2);
				};
			}).get().bestIndex;
		}
		return null;
	}

	
}
