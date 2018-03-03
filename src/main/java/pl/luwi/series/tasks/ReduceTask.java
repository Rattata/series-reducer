package pl.luwi.series.tasks;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import pl.luwi.series.reducer.OrderedPoint;
import pl.luwi.series.reducer.PointSegment;

public class ReduceTask extends CommunicatingThread<LinkedBlockingQueue<PointSegment>, LinkedBlockingQueue<PointSegment>> {
	
	double epsilon;
	ConcurrentHashMap<Integer, OrderedPoint<?>> results;
	
	public ReduceTask(LinkedBlockingQueue<PointSegment> inputQueue, LinkedBlockingQueue<PointSegment> outputQueue,  double epsilon, ConcurrentHashMap<Integer, OrderedPoint<?>> results) {
		super(inputQueue, outputQueue);
		this.epsilon = epsilon;
		this.results = results;
	}

	@Override
	public void process(LinkedBlockingQueue<PointSegment> inputQueue, LinkedBlockingQueue<PointSegment> outputQueue)
			throws Exception {
		PointSegment segment = null;
		while ((segment = inputQueue.poll(timeOut, TimeUnit.MICROSECONDS)) != null){
			if(segment.bestdistance > epsilon){
				for (PointSegment pointSegment : segment.split()) {
					outputQueue.add(pointSegment);
				}
			} else {
				for (OrderedPoint<?> point : segment.asList()) {
					results.put(point.getIndex(), point);					
				}
			}
		}
		
	}

}
