package pl.luwi.series.tasks;

import pl.luwi.series.reducer.Point;
import pl.luwi.series.reducer.PointSegment;

public class FindMaximum<P extends Point> {
	PointSegment<P> segment;
	int startIndex;
	int endIndex;
	double bestDistance;
	int bestIndex;
}
