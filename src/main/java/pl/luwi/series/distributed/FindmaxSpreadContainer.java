package pl.luwi.series.distributed;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.ReentrantLock;

public class FindmaxSpreadContainer {
	ReentrantLock lock = new ReentrantLock();

	HashMap<Integer, List<TaskFindMax>> findmaxstore = new HashMap<>();
	
	public List<TaskFindMax> storeFindMax(TaskFindMax findmax){
		lock.lock();
		List<TaskFindMax> store = findmaxstore.get(findmax.spreadID);
		if(store == null){
			store = new ArrayList<>();
			findmaxstore.put(findmax.spreadID, store);
		}
		store.add(findmax);

		List<TaskFindMax> returnList = null;
		if(store.size() == findmax.totalSegments){
			returnList = store;
			findmaxstore.remove(findmax.spreadID);
		}
		lock.unlock();
		
		return returnList;
	}
}
