package pl.luwi.series.reducer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PointSegment<N extends Point> extends Line<OrderedPoint<N>>{
	
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
	
	public PointSegment(List<OrderedPoint<N>> points, Line<OrderedPoint<N>> line) {
		super(line);
		this.points = points;
	}
	
	public List<PointSegment< N>> split(PointSegment<N> segment,  int index){
		
		List<PointSegment<N>> subsegments = new ArrayList<PointSegment< N>>(2);
		
		PointSegment<N> split1 = new PointSegment<N>(originalpoints.subList(0, index +1 ));
		subsegments.add(split1);
		
		PointSegment<N> split2 = new PointSegment< N>(originalpoints.subList(index, originalpoints.size()));
		subsegments.add(split2);
		
		return subsegments;
	}
	
}
