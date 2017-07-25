package pl.luwi.series.tasks;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import pl.luwi.series.reducer.*;

public class ReduceResult<P extends Point> {
	public ConcurrentHashMap<Integer,P> newpoints = new ConcurrentHashMap<>();
	public int depth = 0;
}
