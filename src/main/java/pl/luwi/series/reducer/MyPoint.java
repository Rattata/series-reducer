package pl.luwi.series.reducer;

import java.util.Random;

public class MyPoint implements Point {
	
	double X;
	double Y;
	
	public MyPoint() {
		Random r = new Random();
		X = (double)2000* r.nextFloat();
		Y = (double)2000* r.nextFloat();
	}
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
	
}