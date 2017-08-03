package pl.luwi.series.tasks;

import pl.luwi.series.reducer.Point;
import pl.luwi.series.reducer.PointSegment;

public class FindMaximumTask {
	int trace = -1;

	int totalTasks = -1;
	PointSegment segment;
	int startIndex;
	int endIndex;
	double bestDistance;
	int bestIndex = -1;
}
