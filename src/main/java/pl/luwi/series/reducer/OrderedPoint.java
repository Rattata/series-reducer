package pl.luwi.series.reducer;

public class OrderedPoint implements Point {

	public Point originalPoint;
	private int Index;
	
	public OrderedPoint(Point point, int index) {
		this.Index = index;
		this.originalPoint = point;
	}
	
	public double getX() {
		return originalPoint.getX();
	}

	public double getY() {
		return originalPoint.getY();
	}

	public int GetIndex() {
		return Index;
	}
	
}
