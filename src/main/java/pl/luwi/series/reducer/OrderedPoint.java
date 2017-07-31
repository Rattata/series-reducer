package pl.luwi.series.reducer;

public class OrderedPoint<P extends Point> implements Point, Comparable<OrderedPoint<P>> {
	private P point;

	private int index;
	
	public OrderedPoint(P point, int index){
		this.point = point;
		this.index = index;
	}
	
	public P getPoint() {
		return point;
	}
	
	public double getX() {
		return point.getX();
	}

	public double getY() {
		return point.getY();
	}
	
	public int getIndex() {
		return index;
	}

	@Override
	public int compareTo(OrderedPoint<P> o) {
		return Integer.compare(index, o.getIndex());
	}
	
}
