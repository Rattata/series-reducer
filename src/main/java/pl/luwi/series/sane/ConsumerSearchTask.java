package pl.luwi.series.sane;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import pl.luwi.series.sane.SearchTask.SearchSegment;

public class ConsumerSearchTask implements Runnable, IStoppable {

	private static LinkedBlockingQueue<SearchSegment> queue = new LinkedBlockingQueue<>();
	
	public void process() throws InterruptedException {
		SearchSegment task = queue.poll(100, TimeUnit.NANOSECONDS);
		if(task == null){return;}
		task.search();
	}

	public static LinkedBlockingQueue<SearchSegment> getQueue(){
		return queue;
	};

	@Override
	public void stop() {
		stop = true;
	}
	private boolean stop = false;

	@Override
	public void run() {
		while (!stop | !queue.isEmpty()) {
			try {
				process();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		System.out.println("stopping searcher");
	}


}
