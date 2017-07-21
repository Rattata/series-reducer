package pl.luwi.series.tasks;

import java.util.List;
import java.util.concurrent.Future;

import pl.luwi.series.reducer.Point;

public class FindMaximumCollectResult<P extends Point> {
	List<Future<FindMaximumResult<P>>> futures;
}
