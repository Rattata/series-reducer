package pl.luwi.series.distributed;

import java.io.Serializable;

import pl.luwi.series.reducer.EpsilonHelper;

public class TaskFindMax  implements Serializable {
	public OrderedPoint[] points;
	public OrderedPoint start,end;
	public int totalSegments, spreadID, lineID, segment;
	
	public double furthestDistance;
	public int maximumIndex;
	public Integer calculationIdentifier;
	public double dx;
	public double dy;
	public double sxey;
	public double exsy;
	public double length;


	public TaskFindMax(OrderedPoint[] points, Line line, int calculationID, int spreadID, int totalSegments, int segment) {
		this.points = points;
		this.totalSegments = totalSegments;
		this.lineID = line.lineID;
		this.spreadID = spreadID;
		this.segment = segment;
		this.calculationIdentifier = calculationID; 
		this.start = line.start;
		this.end = line.end;
		this.dx = line.dx;
		this.dy = line.dy;
		this.exsy = line.exsy;
		this.sxey = line.sxey;
		this.length = line.length;
	}


	public void execute() {
		for (OrderedPoint orderedPoint : points) {
			double distance =  Math.abs(dy * orderedPoint.X - dx * orderedPoint.Y + sxey - exsy) / length;
			if(distance > furthestDistance){
				this.furthestDistance = distance;
				this.maximumIndex = orderedPoint.order;
			}
			
		}
	}
}
