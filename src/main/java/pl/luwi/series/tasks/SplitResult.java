package pl.luwi.series.tasks;

import java.util.List;

import pl.luwi.series.reducer.OrderedPoint;
import pl.luwi.series.reducer.Point;
import pl.luwi.series.reducer.PointSegment;

public class SplitResult<P extends Point> {
	public List<PointSegment<P>> newsegments;
	public List<OrderedPoint<P>> newpoints;
}
