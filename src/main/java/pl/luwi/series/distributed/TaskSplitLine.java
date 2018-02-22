package pl.luwi.series.distributed;

import java.io.Serializable;

public class TaskSplitLine implements Serializable {
	public Line line;
	public double epsilon;
	public int index;
	
	public TaskSplitLine(Line line, double epsilon, int index) {
		this.line = line;
		this.epsilon = epsilon;
		this.index = index;
	}
	
}
