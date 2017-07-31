package pl.luwi.series.tasks;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import pl.luwi.series.reducer.PointSegment;

public class FindMaximumGather extends CommunicatingThread<LinkedBlockingQueue<List<FindMaximum>>, LinkedBlockingQueue<PointSegment>> {
	
	public FindMaximumGather(LinkedBlockingQueue<List<FindMaximum>> input, LinkedBlockingQueue<PointSegment> output) {
		super(input, output);
		
	}

	@Override
	public void process(LinkedBlockingQueue<List<FindMaximum>> inputQueue,
			LinkedBlockingQueue<PointSegment> outputQueue) throws Exception {
		List<FindMaximum> findMaximumTasks = null;
		while ((findMaximumTasks = inputQueue.poll(timeOut, TimeUnit.MICROSECONDS)) != null){
			FindMaximum maximum = findMaximumTasks.stream().max(pointComparator).get();
			PointSegment returnSegment = maximum.segment;
			returnSegment.bestdistance = maximum.bestDistance;
			returnSegment.maximum = maximum.bestIndex;
			outputQueue.add(returnSegment);
		}
	}
	
	Comparator<FindMaximum> pointComparator = new Comparator<FindMaximum>() {
		
		@Override
		public int compare(FindMaximum o1, FindMaximum o2) {
			return Double.compare(o1.bestDistance, o2.bestDistance);
		}
	};
//
//	@Override
//	public boolean IsDone() {
//		return inputQueue.isEmpty() && outputQueue.isEmpty() && ! this.getCurrentState().equals(STATE.RUNNING);
//	}

}
