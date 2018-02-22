package pl.luwi.series.distributed;

import java.io.Serializable;

public class TaskResult implements Serializable {
	Integer calculationIdentifier;
	OrderedPoint start;
	OrderedPoint end;
	int lineID;
	public TaskResult(Integer calculationIdentifier,int lineID, OrderedPoint start, OrderedPoint end) {
		this.start = start;
		this.end = end;
		this.calculationIdentifier = calculationIdentifier;
		this.lineID = lineID;
	}
}
