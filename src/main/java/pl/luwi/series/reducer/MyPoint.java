package pl.luwi.series.reducer;

import java.util.Random;

public class MyPoint implements Point {
	
	double X;
	double Y;
	
	public MyPoint() {
		Random r = new Random();
		X = (double)100* r.nextFloat();
		Y = (double)100* r.nextFloat();
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
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "["+X+","+Y+"]";
	}
	
}