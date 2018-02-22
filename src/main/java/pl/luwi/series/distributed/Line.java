package pl.luwi.series.distributed;

import java.io.Serializable;
import java.util.Arrays;

public class Line implements Serializable {

	public Integer calculationIdentifier;
	public int lineID;
	public OrderedPoint[] points;
	OrderedPoint start, end;

	public double dx;
	public double dy;
	public double sxey;
	public double exsy;
	public double length;

	public Line(Integer calculationIdentifier, OrderedPoint[] points, int lineID) {
		this.calculationIdentifier = calculationIdentifier;
		this.start = points[0];
		this.end = points[points.length - 1];
		this.points = Arrays.copyOfRange(points, 1, points.length - 2);
		dx = start.X - end.X;
		dy = start.Y - end.Y;
		sxey = start.X * end.Y;
		exsy = end.X * start.Y;
		length = Math.sqrt(dx * dx + dy * dy);
		this.lineID = lineID;
	}

	public Line(Integer calculationIdentifier,OrderedPoint[] points, OrderedPoint start, OrderedPoint end,  int lineID) {
		this.calculationIdentifier = calculationIdentifier;
		this.start = start;
		this.end = end;
		this.points = points;
		dx = start.X - end.X;
		dy = start.Y - end.Y;
		sxey = start.X * end.Y;
		exsy = end.X * start.Y;
		length = Math.sqrt(dx * dx + dy * dy);
		this.lineID = lineID;
	}

	public Line[] split(int order, int aLineID, int bLineID) {
		int index = order - points[0].order;
		try {
			OrderedPoint[] pointsA = index - 1 < 0 ?  new OrderedPoint[0] : Arrays.copyOfRange(points, 0, index - 1);
			OrderedPoint[] pointsB = index + 1 > points.length -1 ?  new OrderedPoint[0]: Arrays.copyOfRange(points, index + 1, points.length - 1);
			OrderedPoint newPoint = points[index];
			return new Line[] { new Line( this.calculationIdentifier, pointsA, start, newPoint, aLineID),
					new Line(this.calculationIdentifier, pointsB, newPoint, end,  bLineID) };
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}

}
