package pl.luwi.series.tasks;

import java.io.Serializable;

import pl.luwi.series.reducer.PointSegment;

public class FindMaximum implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public PointSegment segment;
	public int startIndex;
	public int endIndex;
	public double bestDistance;
	public int bestIndex;

	public double dx;
	public double dy;
	public double sxey;
	public double exsy;
	public double length;
}
