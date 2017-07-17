package pl.luwi.series.reducer;

import java.util.ArrayList;
import java.util.List;

public class OrderedLineSegment extends Line<OrderedPoint> {
	
	private List<OrderedPoint> points;
	
	public List<OrderedPoint> getPoints() {
		return points;
	}
	
	public <P extends Point> OrderedLineSegment(List<P> points) {
		super(
				new OrderedPoint(points.get(0), 0), 
				new OrderedPoint(points.get(points.size() - 1 ), points.size() - 1)
			);
		this.points = new ArrayList<OrderedPoint>(points.size());
		for (int i = 0 ; i < points.size(); i++) {
			this.points.add(new OrderedPoint(points.get(i), i));
		}
	}
	
	public OrderedLineSegment(List<OrderedPoint> points, OrderedPoint start, OrderedPoint end) {
		super(start, end);
		this.points = points;
	}
	
}
