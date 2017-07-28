package pl.luwi.series.tasks;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import pl.luwi.series.reducer.Point;
import pl.luwi.series.reducer.PointSegment;

public class FindMaximumProcessor<P extends Point> extends Process {

	LinkedBlockingQueue<List<FindMaximum<P>>> input;
	public LinkedBlockingQueue<List<FindMaximum<P>>> output;

	int threadId;

	public FindMaximumProcessor(int threadId, Process master, LinkedBlockingQueue<List<FindMaximum<P>>> input,
			LinkedBlockingQueue<List<FindMaximum<P>>> output) {
		super(master);
		this.threadId = threadId;
		this.input = input;
		this.output = output;
	}

	@Override
	public void run() {
		List<FindMaximum<P>> findTaskList;
		try {
			while (!isMasterDone()) {

				while ((findTaskList = input.poll(ConcurrentConstants.TIMEOUT_NS, TimeUnit.MICROSECONDS)) != null) {
					if (findTaskList == null)
						continue;
					System.out.println(this.getClass().getName());
					FindMaximum<P> task = findTaskList.get(threadId);
					getMax(task);
					output.add(findTaskList);
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		isDone = true;
		
	}

	
	
	public void getMax(FindMaximum<P> findmax) {
		PointSegment<P> segment = findmax.segment;
		for (int i = findmax.startIndex; i <= findmax.endIndex; i++) {
			double dist = segment.distance(segment.points.get(i));
			if (dist > findmax.bestDistance) {
				findmax.bestDistance = dist;
				findmax.bestIndex = i;

			}
		}
	}

}
