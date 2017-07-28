package pl.luwi.series.tasks;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import pl.luwi.series.reducer.OrderedPoint;
import pl.luwi.series.reducer.Point;
import pl.luwi.series.reducer.PointSegment;
import static pl.luwi.series.tasks.ConcurrentConstants.*;

public class FindMaximumSpreadGather<P extends Point> extends Process {

	public LinkedBlockingQueue<PointSegment<P>> inputQueue;
	public LinkedBlockingQueue<PointSegment<P>> outputQueue;
	public LinkedBlockingQueue<List<FindMaximum<P>>> spreadQueue;
	public LinkedBlockingQueue<List<FindMaximum<P>>> gatherQueue;

	public double bestDistance = Double.MIN_VALUE;
	public int bestIndex = -1;
	private int threads;
	private List<Thread> processors;

	public FindMaximumSpreadGather(int threads, Process master, LinkedBlockingQueue<PointSegment<P>> inputqueue,
			LinkedBlockingQueue<PointSegment<P>> outputQueue) {
		super(master);
		this.threads = threads;
		processors = new ArrayList<>();
		this.inputQueue = inputqueue;
		this.outputQueue = outputQueue;
		spreadQueue = new LinkedBlockingQueue<>();

		// arrange queues in series
		LinkedBlockingQueue<List<FindMaximum<P>>> subinputQueue = spreadQueue;
		LinkedBlockingQueue<List<FindMaximum<P>>> subexitQueue = null;
		for (int i = 0; i < threads; i++) {
			subexitQueue = new LinkedBlockingQueue<>();
			FindMaximumProcessor<P> processor = new FindMaximumProcessor<>(i, this, subinputQueue, subexitQueue);
			subinputQueue = processor.output;
			gatherQueue = processor.output;

			processors.add(new Thread(processor));
		}
	}

	@Override
	public void run() {
		try {
			// start processors
			processors.stream().forEach(x -> x.start());

			PointSegment<P> segment;
			List<FindMaximum<P>> findmax;
			while (!isMasterDone()) {

				while ((segment = inputQueue.poll(TIMEOUT_NS, TimeUnit.MICROSECONDS)) != null
						| (findmax = gatherQueue.poll(TIMEOUT_NS, TimeUnit.MICROSECONDS)) != null) {
					if (segment != null || findmax != null)
						System.out.println(this.getClass().getName());

					if (segment != null) {
						double delta = segment.points.size() / threads;
						List<FindMaximum<P>> taskStack = new ArrayList<>();
						for (int i = 0; i < threads; i++) {
							FindMaximum<P> task = new FindMaximum<>();
							task.segment = segment;
							task.startIndex = (int) Math.ceil(delta * i);
							task.endIndex = (int) Math.ceil(delta * (i + 1)) - 1;
							taskStack.add(task);
						}
						spreadQueue.put(taskStack);
					}

					if (findmax != null) {
						FindMaximum<P> maximum = findmax.stream().max(comparator).get();
						PointSegment<P> outputsegment = maximum.segment;
						outputsegment.bestdistance = maximum.bestDistance;
						outputsegment.maximum = maximum.bestIndex;
						outputQueue.add(outputsegment);
					}

				}
			}
			isDone = true;
			for (Thread thread : processors) {
				thread.join();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	Comparator<FindMaximum<P>> comparator = new Comparator<FindMaximum<P>>() {
		@Override
		public int compare(FindMaximum<P> o1, FindMaximum<P> o2) {
			return Double.compare(o1.bestDistance, o2.bestDistance);
		}
	};

	@Override
	public boolean isDone() {
		return inputQueue.isEmpty() && outputQueue.isEmpty() && gatherQueue.isEmpty() && spreadQueue.isEmpty();
	}

}
