package pl.luwi.series.distributed;

import java.io.Serializable;

import pl.luwi.series.reducer.Point;

public class OrderedPoint implements Serializable {
	public double X;
	public double Y;
	public int order;
	
	public OrderedPoint(Point p, int order) {
		this.X = p.getX();
		this.Y = p.getY();
		this.order = order;
	}
}
