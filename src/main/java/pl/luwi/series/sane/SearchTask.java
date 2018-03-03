package pl.luwi.series.sane;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;

import pl.luwi.series.sane.SearchTask.SearchSegment;

public class SearchTask {
	
	private OrderedLine<?> line;

	private CountDownLatch latch;
	
	private List<SearchSegment> segments = new ArrayList();
	
	private LinkedBlockingQueue<SearchSegment> produceTo;
	
	
	public SearchTask(OrderedLine<?> line, int parts){
		produceTo = ConsumerSearchTask.getQueue();
		this.line = line;
		latch = new CountDownLatch(parts);
		int chunkSize = (int) Math.ceil(line.points.size() / parts);
		for (int j = 0; j < parts; j++) {
			int start = j * chunkSize;
			int end = (j +1 ) * chunkSize;
			SearchSegment segment = new SearchSegment(this, start, end);
			segments.add(segment);
		}
	}
	
	public OrderedLine<?> getLine() {
		return line;
	}
	
	public SearchResult doSearch() throws InterruptedException{
		SearchSegment idothisone = segments.get(0);
		for (SearchSegment searchSegment : segments.subList(1, segments.size())) {
			produceTo.put(searchSegment);
		}
		idothisone.search();
		latch.await();
		return segments.stream().map(x-> x.getResult()).max(comparator).get();
	};
	
	
	public class SearchSegment {
		private SearchTask master;
		
		double furthestDistance = Double.MIN_VALUE;
		int furthestIndex = -1;

		int startIndex;
		int endIndex;
		
		public SearchSegment(SearchTask master, int startIndex, int endIndex) {
			this.master = master;
			this.startIndex = startIndex;
			this.endIndex = endIndex;
		}
		
		public SearchResult getResult(){
			return new SearchResult(furthestDistance, furthestIndex);
		}
		
		public void search(){
			for (int i = startIndex; i <= endIndex && i < master.line.points.size(); i++) {
				double distance = master.line.distance(master.line.points.get(i));
				if (distance > furthestDistance) {
					furthestDistance = distance;
					furthestIndex = i;
				}
			}
			master.latch.countDown();
		};
	}
	
	public class SearchResult {
		public double furthestDistance = Double.MIN_VALUE;
		int furthestIndex = -1;
		public SearchResult(double furthestDistance, int furthestIndex) {
			this.furthestDistance = furthestDistance;
			this.furthestIndex = furthestIndex;
		}
	}
	
	public final static Comparator<SearchResult> comparator = new Comparator<SearchResult>() {

		@Override
		public int compare(SearchResult o1, SearchResult o2) {
			// TODO Auto-generated method stub
			return ((int) o1.furthestDistance) - ((int) o2.furthestDistance);
		}
	};
	
}