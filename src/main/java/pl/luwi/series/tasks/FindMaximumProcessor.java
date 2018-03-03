package pl.luwi.series.tasks;

import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.jfree.util.WaitingImageObserver;

import static pl.luwi.series.tasks.ConcurrentConstants.TIMEOUT_uS;

import pl.luwi.series.reducer.Point;
import pl.luwi.series.reducer.PointSegment;

public class FindMaximumProcessor extends CommunicatingThread<LinkedBlockingQueue<List<FindMaximum>>,LinkedBlockingQueue<List<FindMaximum>>>{

	private int id;

	public FindMaximumProcessor(LinkedBlockingQueue<List<FindMaximum>> inputQueue,
			LinkedBlockingQueue<List<FindMaximum>> outputQueue, int id, List<CommunicatingThread<?, ?>> peers) {
		super(inputQueue, outputQueue);
		this.id = id;
		for (CommunicatingThread<?, ?> communicatingThread : peers) {
			addPeer(communicatingThread);
		}
	}

	@Override
	public void process(LinkedBlockingQueue<List<FindMaximum>> inputQueue,
			LinkedBlockingQueue<List<FindMaximum>> outputQueue) throws Exception {
		List<FindMaximum> maximum = null;
		while((maximum = inputQueue.poll(timeOut, TimeUnit.MICROSECONDS))!= null){
			FindMaximum task = maximum.get(id);
			FindMaximumInRange(task, task.startIndex, task.endIndex);
			outputQueue.put(maximum);
		}
	}
//
//	@Override
//	public boolean IsDone() {
//		return ! this.getCurrentState().equals(STATE.RUNNING) && inputQueue.isEmpty() && outputQueue.isEmpty();
//	};
	
	private void FindMaximumInRange(FindMaximum findTask, int startRange, int endRange){
		PointSegment segment = findTask.segment;
		for(int i = startRange; i <= endRange; i++){
			double distance = segment.distance(segment.points.get(i));
			
			if(distance > findTask.bestDistance){
				findTask.bestDistance = distance;
				findTask.bestIndex = i;
			}
		}
		
	} 

	
}
