package pl.luwi.series.tasks;

import pl.luwi.series.reducer.OrderedPoint;
import pl.luwi.series.reducer.Point;

public class FindMaximumResult<T extends Point> implements Comparable<FindMaximumResult<T>> {
	OrderedPoint<T> result;
	double furthestDistance = Double.MIN_VALUE;
	
	@Override
	public int compareTo(FindMaximumResult<T> o) {
		return Double.compare(furthestDistance, o.furthestDistance);
	}
}
