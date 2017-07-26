package pl.luwi.series.reducer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PointSegment<N extends Point> extends Line<OrderedPoint<N>> implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	public List<OrderedPoint<N>> points;
	public List<N> originalpoints;
	
	public PointSegment(List<N> points) {
		super(new OrderedPoint<N>(points.get(0), 0), new OrderedPoint<N>(points.get(points.size() -1 ), points.size() -1 ));
		this.points = new ArrayList<>();
		for(int i = 0 ; i < points.size(); i++) {
			this.points.add(new OrderedPoint<N>(points.get(i), i));
		};
		originalpoints = points;
	};
	
	public PointSegment(List<OrderedPoint<N>> points, OrderedPoint<N> start, OrderedPoint<N> end) {
		super(start, end);
		this.points = points;
	}
	
	public List<PointSegment< N>> split( int index){
		
		List<PointSegment<N>> subsegments = new ArrayList<PointSegment< N>>(2);
		OrderedPoint<N> start = points.get(0); 
		OrderedPoint<N> middle = points.get(index);
		OrderedPoint<N> end = points.get(points.size() -1);
		
		PointSegment<N> split1 = new PointSegment<N>(points.subList(0, index +1 ), start, middle);
		subsegments.add(split1);
		
		PointSegment<N> split2 = new PointSegment< N>(points.subList(index, points.size()), middle, end);
		subsegments.add(split2);
		
		return subsegments;
	}
	
}
