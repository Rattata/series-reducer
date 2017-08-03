package pl.luwi.series.tasks;

import static pl.luwi.series.tasks.ConcurrentConstants.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import pl.luwi.series.reducer.PointSegment;

public class FindMaximumSpreader
		extends CommunicatingThread<LinkedBlockingQueue<PointSegment>, LinkedBlockingQueue<List<FindMaximumTask>>> {

	private List<FindMaximumProcessor> processors = new ArrayList<>();
	private ConcurrentHashMap<Integer, FindMaximumResult> runningTasks = null;
	
	private int size;
	private int lastTrace = 0;
	
	public FindMaximumSpreader(LinkedBlockingQueue<PointSegment> input, LinkedBlockingQueue<List<FindMaximumTask>> output, FindMaximumGather gatherer, int threads, List<FindMaximumProcessor> processors) {
		super(input, output);
		addPeer(gatherer);
		this.processors = processors;
		this.size = processors.size();
		runningTasks = new ConcurrentHashMap<>();
	}

	@Override
	public void process(LinkedBlockingQueue<PointSegment> inputQueue,
			LinkedBlockingQueue<List<FindMaximumTask>> outputQueue) throws Exception {
		PointSegment segment;

		while ((segment = inputQueue.poll(timeOut, TimeUnit.MICROSECONDS)) != null) {
			double delta = segment.points.size() / size;
			int chunks = (int)Math.ceil(segment.points.size() / FINDMAX_CHUNK_SIZE);
			List<FindMaximumTask> findMAxTasks = new ArrayList<>(chunks);
			FindMaximumResult result = new FindMaximumResult();
			result.totalItems = chunks;
			result.tasks = new FindMaximumTask[chunks];
			result.trace = lastTrace++;
			for(int j = 0 ; j <= chunks ; j++) {
				FindMaximumTask task = new FindMaximumTask();
				task.segment = segment;
				task.startIndex = 0 + FINDMAX_CHUNK_SIZE * j;
				task.endIndex = 0 + FINDMAX_CHUNK_SIZE * (j + 1);
				result.tasks[j] = task;
			}
			runningTasks.put(result.trace, result);
			List<FindMaximumTask> taskList = new ArrayList<>();
			for (int i = 0; i < size; i++) {
				FindMaximumTask task = new FindMaximumTask();
				task.segment = segment;
				task.startIndex = (int) Math.ceil(delta * i);
				task.endIndex = (int) Math.ceil(delta * (i + 1)) - 1;
				taskList.add(task);
			}
			outputQueue.add(taskList);
		}

	}

}
