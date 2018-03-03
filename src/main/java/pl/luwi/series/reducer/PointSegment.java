package pl.luwi.series.reducer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PointSegment extends Line<OrderedPoint<?>> implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	public List<OrderedPoint<?>> points;
	public int maximum = -1;
	public double bestdistance = Double.MIN_VALUE; 
	
	public <P extends Point>PointSegment(List<P> points) {
		super(new OrderedPoint<P>(points.get(0), 0), new OrderedPoint<P>(points.get(points.size() -1 ), points.size() -1 ));
		this.points = new ArrayList<>();
		for(int i = 0 ; i < points.size(); i++) {
			this.points.add(new OrderedPoint<P>(points.get(i), i));
		};
		
	};
	
	public PointSegment(List<OrderedPoint<?>> points, OrderedPoint<?> start, OrderedPoint<?> end) {
		super(start, end);
		this.points = points;
	}
	
	public List<PointSegment> split(){
		
		List<PointSegment> subsegments = new ArrayList<PointSegment>(2);
		OrderedPoint<?> start = points.get(0); 
		OrderedPoint<?> middle = points.get(maximum);
		OrderedPoint<?> end = points.get(points.size() -1);
		
		PointSegment split1 = new PointSegment(points.subList(0, maximum +1 ), start, middle);
		subsegments.add(split1);
		
		PointSegment split2 = new PointSegment(points.subList(maximum, points.size()), middle, end);
		subsegments.add(split2);
		
		return subsegments;
	}
	
}
