package pl.luwi.series.tasks;

import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.jfree.util.WaitingImageObserver;

import static pl.luwi.series.tasks.ConcurrentConstants.TIMEOUT_uS;

import pl.luwi.series.reducer.Point;
import pl.luwi.series.reducer.PointSegment;

public class FindMaximumProcessor extends CommunicatingThread<LinkedBlockingQueue<List<FindMaximumTask>>,LinkedBlockingQueue<List<FindMaximumTask>>>{

	private int id;

	public FindMaximumProcessor(LinkedBlockingQueue<List<FindMaximumTask>> inputQueue,
			LinkedBlockingQueue<List<FindMaximumTask>> outputQueue, int id, List<CommunicatingThread<?, ?>> peers) {
		super(inputQueue, outputQueue);
		this.id = id;
		for (CommunicatingThread<?, ?> communicatingThread : peers) {
			addPeer(communicatingThread);
		}
	}

	@Override
	public void process(LinkedBlockingQueue<List<FindMaximumTask>> inputQueue,
			LinkedBlockingQueue<List<FindMaximumTask>> outputQueue) throws Exception {
		List<FindMaximumTask> maximum = null;
		while((maximum = inputQueue.poll(timeOut, TimeUnit.MICROSECONDS))!= null){
			
			List<FindMaximumTask> unfinishedtask = maximum.stream().filter((x) -> x.bestIndex == -1).collect(Collectors.toList());
			
			FindMaximumTask task = unfinishedtask.get(0);
			FindMaximumInRange(task, task.startIndex, task.endIndex);
//			was last item in list
			if(unfinishedtask.size() == 1) {
				outputQueue.put(maximum);
			} else {
				inputQueue.put(maximum);				
			}
		}
	}
//
//	@Override
//	public boolean IsDone() {
//		return ! this.getCurrentState().equals(STATE.RUNNING) && inputQueue.isEmpty() && outputQueue.isEmpty();
//	};
	
	private void FindMaximumInRange(FindMaximumTask findTask, int startRange, int endRange){
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
