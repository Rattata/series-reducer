package pl.luwi.series.tasks;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import pl.luwi.series.reducer.PointSegment;

public class FindMaximumGather extends CommunicatingThread<LinkedBlockingQueue<FindMaximumTask>, LinkedBlockingQueue<PointSegment>> {
	
	LinkedBlockingQueue<FindMaximumResult> TraceInput = new LinkedBlockingQueue<>();
	ConcurrentHashMap<Integer, FindMaximumResult> resultCache;
	public FindMaximumGather(LinkedBlockingQueue<FindMaximumTask> input, LinkedBlockingQueue<PointSegment> output, ConcurrentHashMap<Integer , FindMaximumResult> resultcache) {
		super(input, output);
		
	}

	@Override
	public void process(LinkedBlockingQueue<FindMaximumTask> inputQueue,
			LinkedBlockingQueue<PointSegment> outputQueue) throws Exception {
		FindMaximumTask localMaximum = null;
		while ((localMaximum = inputQueue.poll(timeOut, TimeUnit.MICROSECONDS)) != null){
			FindMaximumResult result = resultCache.get(localMaximum.trace);
			
			
			if(result.tasks.size() == result.totalItems) {
				FindMaximumTask furthest  = result.tasks.stream().max(pointComparator).get();
				PointSegment returnSegment = furthest.segment;
				returnSegment.bestdistance = furthest.bestDistance;
				returnSegment.maximum = furthest.bestIndex;
				outputQueue.add(returnSegment);
			}
			
		}
	}
	
	Comparator<FindMaximumTask> pointComparator = new Comparator<FindMaximumTask>() {
		
		@Override
		public int compare(FindMaximumTask o1, FindMaximumTask o2) {
			return Double.compare(o1.bestDistance, o2.bestDistance);
		}
	};
//
//	@Override
//	public boolean IsDone() {
//		return inputQueue.isEmpty() && outputQueue.isEmpty() && ! this.getCurrentState().equals(STATE.RUNNING);
//	}

}
