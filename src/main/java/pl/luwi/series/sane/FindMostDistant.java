package pl.luwi.series.sane;

import static pl.luwi.series.sane.Constants.COMPUTATION_SEARCH_SEGMENT_SIZE;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import pl.luwi.series.sane.FindMostDistant.SearchResult;

public class FindMostDistant {
	static class SearchResult {
		public double furthestDistance = Double.MIN_VALUE;
		int furthestIndex = -1;

		public static Comparator<SearchResult> comparator = new Comparator<FindMostDistant.SearchResult>() {

			@Override
			public int compare(SearchResult o1, SearchResult o2) {
				// TODO Auto-generated method stub
				return ((int) o1.furthestDistance) - ((int) o2.furthestDistance);
			}
		};
	}

	public static SearchResult searchTasks(OrderedLine<?> line, ExecutorService executor) throws InterruptedException {
		List<Callable<SearchResult>> findTasks = new ArrayList<>();
		SearchResult topresult = null;

		int segments = (int) Math.ceil(line.points.size() / COMPUTATION_SEARCH_SEGMENT_SIZE);
		for (int j = 0; j < segments; j++) {

			final int var = j;

			Callable<SearchResult> task = () -> {
				SearchResult result = new SearchResult();
				int start = (int) Math.floor(var * COMPUTATION_SEARCH_SEGMENT_SIZE);
				int end = (int) Math.ceil(var + 1 * COMPUTATION_SEARCH_SEGMENT_SIZE);
				end = end >= line.points.size() ? line.points.size() : end;
				for (int i = start; i < end; i++) {
					double distance = line.distance(line.points.get(i));
					if (distance > result.furthestDistance) {
						result.furthestDistance = distance;
						result.furthestIndex = i;
					}
				}
				return result;
			};
			findTasks.add(task);
		}

		topresult = executor.invokeAll(findTasks)
				.stream().map(x -> {
			try {
				return x.get();
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}).max(SearchResult.comparator).get();

		return topresult;
	}
}
