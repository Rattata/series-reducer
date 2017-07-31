package pl.luwi.series.tasks;

import static pl.luwi.series.tasks.ConcurrentConstants.TIMEOUT_uS;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import pl.luwi.series.reducer.PointSegment;

public class FindMaximumSpreader
		extends CommunicatingThread<LinkedBlockingQueue<PointSegment>, LinkedBlockingQueue<List<FindMaximum>>> {

	private List<FindMaximumProcessor> processors = new ArrayList<>();
	private int size;
	
	public FindMaximumSpreader(LinkedBlockingQueue<PointSegment> input, LinkedBlockingQueue<List<FindMaximum>> output, FindMaximumGather gatherer, int threads, List<FindMaximumProcessor> processors) {
		super(input, output);
		addPeer(gatherer);
		this.processors = processors;
		this.size = processors.size();
	}

	@Override
	public void process(LinkedBlockingQueue<PointSegment> inputQueue,
			LinkedBlockingQueue<List<FindMaximum>> outputQueue) throws Exception {
		PointSegment segment;

		while ((segment = inputQueue.poll(timeOut, TimeUnit.MICROSECONDS)) != null) {
			double delta = segment.points.size() / size;
			List<FindMaximum> taskStack = new ArrayList<>();
			for (int i = 0; i < size; i++) {
				FindMaximum task = new FindMaximum();
				task.segment = segment;
				task.startIndex = (int) Math.ceil(delta * i);
				task.endIndex = (int) Math.ceil(delta * (i + 1)) - 1;
				taskStack.add(task);
			}
			outputQueue.add(taskStack);
		}

	}

}
