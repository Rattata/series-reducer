package pl.luwi.series.tasks;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import pl.luwi.series.reducer.*;

public class ReduceResult<P extends Point> {
	public List<OrderedPoint<P>> filteredPoints = new ArrayList<>();
	public List<PointSegment<P>> segments = new ArrayList<>();
	
}
