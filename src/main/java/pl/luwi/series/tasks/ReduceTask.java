package pl.luwi.series.tasks;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import pl.luwi.series.reducer.OrderedPoint;
import pl.luwi.series.reducer.Point;
import pl.luwi.series.reducer.PointSegment;

public class ReduceTask<P extends Point> extends Process {

	public LinkedBlockingQueue<PointSegment<P>> inputQueue = new LinkedBlockingQueue<>();
	public LinkedBlockingQueue<PointSegment<P>> outputQueue = new LinkedBlockingQueue<>();
	public LinkedBlockingQueue<OrderedPoint<P>> resultQueue = new LinkedBlockingQueue<>();

	public ReduceResult<P> result;
	double epsilon;

	public ReduceTask(double epsilon, Process master) {
		super(master);
		result = new ReduceResult<>();
		this.epsilon = epsilon;
	}

	@Override
	public void run() {
		try {
			PointSegment<P> segment;
			while (!isMasterDone() && !isDone()) {

				while ((segment = inputQueue.poll(ConcurrentConstants.TIMEOUT_NS, TimeUnit.MICROSECONDS)) != null) {
					if (segment == null)
						continue;
					System.out.println(this.getClass().getName());
					if (segment.bestdistance > epsilon) {

						for (PointSegment<P> newsegment : segment.split()) {
							outputQueue.add(newsegment);
						}

					} else {
						for (OrderedPoint<P> orderedPoint : segment.asList()) {
							resultQueue.add(orderedPoint);
						}
					}
				}
				if(inputQueue.isEmpty() && outputQueue.isEmpty()) {
					isDone = true;
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		isDone = true;
	}

	@Override
	public boolean isDone() {
		return isDone;
	}

}
