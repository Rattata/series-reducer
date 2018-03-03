package pl.luwi.series.distributed;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

public class CalculationContainer<P extends OrderedPoint> {
	public double Epsilon;
	public int ID;
	public CountDownLatch latch = new CountDownLatch(1);
	private ReentrantLock lock = new ReentrantLock();
	
    
	public List<P> point;
	private HashMap<Integer, Object> counter;
	private HashMap<Integer, P> results;

	public void signalResult(Integer lineID) {
		lock.lock();
		counter.put(lineID, new Object());
		lock.unlock();
	}

	/**
	 * 
	 * @return whether result is ready
	 */
	public boolean appendResult(Integer lineID, List<P> result) {
		lock.lock();
		boolean res= false;
		counter.remove(lineID);
		for (P p : result) {
			results.put(p.getI(), p);			
		}
		if(counter.isEmpty()){
			latch.countDown();
			res = true;
		}
		lock.unlock();
		return res;
	}

	public CalculationContainer(int RDPID, double epsilon, TaskOrderedLine<P> line) {
		this.Epsilon = epsilon;
		this.ID = RDPID;
		results = new HashMap<>((int) (line.points.size() * 0.75));
		counter = new HashMap<>((int) (line.points.size() * 0.75));
		counter.put(line.lineID, new Object());
	}
	
	public List<P> results(){
		lock.lock();
		List<P> restuls =results.entrySet().stream().map(x -> x.getValue()).collect(Collectors.toList());
		lock.unlock();
		return restuls;
	}
}
