package pl.luwi.series.distributed;

import java.io.Serializable;

public class IndexedPoint implements OrderedPoint, Serializable{
	double X,Y;
	int Index;
	@Override
	public double getX() {
		// TODO Auto-generated method stub
		return X;
	}

	@Override
	public double getY() {
		// TODO Auto-generated method stub
		return Y;
	}

	@Override
	public int getI() {
		// TODO Auto-generated method stub
		return Index;
	}
	
	public IndexedPoint(double X , double Y, int Index) {
		this.X = X;
		this.Y = Y;
		this.Index = Index;
	}
} 