package pl.luwi.series.reducer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;
import java.util.stream.Collectors;

public class IterativeSeriesReducer {

	private static class Work<P extends Point> {
		Line<P> line;
		List<P> points;

		public Work(Line<P> line, List<P> points) {
			this.line = line;
			this.points = points;
		}
	}

	/**
	 * Reduces number of points in given series using Ramer-Douglas-Peucker
	 * algorithm.
	 * 
	 * @param points
	 *            initial, ordered list of points (objects implementing the
	 *            {@link Point} interface)
	 * @param epsilon
	 *            allowed margin of the resulting curve, has to be > 0
	 */
	// public static <P extends Point> List<P> reduce(List<P> points, double
	// epsilon) {
	// if (epsilon < 0) {
	// throw new IllegalArgumentException("Epsilon cannot be less then 0.");
	// }
	// double furthestPointDistance = 0.0;
	// int furthestPointIndex = 0;
	// Line<P> line = new Line<P>(points.get(0), points.get(points.size() - 1));
	// for (int i = 1; i < points.size() - 1; i++) {
	// double distance = line.distance(points.get(i));
	// if (distance > furthestPointDistance ) {
	// furthestPointDistance = distance;
	// furthestPointIndex = i;
	// }
	// }
	// if (furthestPointDistance > epsilon) {
	// List<P> reduced1 = reduce(points.subList(0, furthestPointIndex+1), epsilon);
	// List<P> reduced2 = reduce(points.subList(furthestPointIndex, points.size()),
	// epsilon);
	// List<P> result = new ArrayList<P>(reduced1);
	// result.addAll(reduced2.subList(1, reduced2.size()));
	// return result;
	// } else {
	// return line.asList();
	// }
	// }

	/**
	 * Reduces number of points in given series using Ramer-Douglas-Peucker
	 * algorithm.
	 * 
	 * @param points
	 *            initial, ordered list of points (objects implementing the
	 *            {@link Point} interface)
	 * @param epsilon
	 *            allowed margin of the resulting curve, has to be > 0
	 */
	public static <P extends Point> List<P> reduce(List<P> points, double epsilon) {
		if (epsilon < 0) {
			throw new IllegalArgumentException("Epsilon cannot be less then 0.");
		}
		Stack<OrderedLineSegment> work = new Stack<OrderedLineSegment>();
		work.push(new OrderedLineSegment(points));
		
		OrderedPoint[] result = new OrderedPoint[points.size()];

		while (!work.isEmpty()) {
			OrderedLineSegment segment = work.pop();
			
			double furthestPointDistance = 0.0;
			int furthestPointIndex = 0;
			for (int i = 0; i < segment.getPoints().size() - 1; i++) {
				double distance = segment.distance(segment.getPoints().get(i));
				if (distance > furthestPointDistance) {
					furthestPointDistance = distance;
					furthestPointIndex = i;
				}
			}
			
			if (furthestPointDistance > epsilon) {
				List<OrderedPoint> firstPoints = segment.getPoints().subList(0, furthestPointIndex + 1); 
				OrderedLineSegment firstSegment = new OrderedLineSegment(firstPoints, segment.getStart(), segment.getPoints().get(furthestPointIndex));
				
				List<OrderedPoint> secondPoints = segment.getPoints().subList(furthestPointIndex, segment.getPoints().size());
				OrderedLineSegment secondSegment = new OrderedLineSegment(secondPoints, segment.getPoints().get(furthestPointIndex), segment.getEnd());
				
				work.push(firstSegment);
				work.push(secondSegment);
	        } else {
	        	OrderedPoint start = segment.getStart();
	        	OrderedPoint end = segment.getEnd();
	        	if(result[end.GetIndex()] == null) {
	        		result[end.GetIndex()] = end;
	        	}
	        	
	        	if( result[start.GetIndex()] == null){
	        		result[start.GetIndex()] = start;
	        	}
	        }
		}
		//collect points
		List<P> reducedPoints = new ArrayList<P>();
		for (int i = 0; i < result.length; i++) {
			if(result[i] != null) {
				reducedPoints.add(points.get(result[i].GetIndex()));
			}
		}
		return reducedPoints;
	}

}
