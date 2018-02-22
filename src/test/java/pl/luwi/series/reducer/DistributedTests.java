package pl.luwi.series.reducer;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.Random;

import javax.jms.JMSException;

import org.testng.annotations.Test;

import pl.luwi.series.distributed.DistributedSeriesReducer;
//
//import pl.luwi.series.distributed.DistributedPoint;
//import pl.luwi.series.distributed.DistributedLineSegment;

public class DistributedTests {
    
	
	@Test
	public void HappyFlow(){
ArrayList<MyPoint> points = new ArrayList<>();
		
		for (int i = 0; i <= 100; i++) {
			points.add(new MyPoint());
		}
		try {
			DistributedSeriesReducer.reduce(points, 1);
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
//    @Test
//    public void SerializationTests() {
//        Random r = new Random();
//        int noPoints = 10;
//        DistributedPoint[] points= new DistributedPoint[noPoints];
//        
//    	for(int i = 0 ; i < noPoints; i++){
//        	points[i] = new DistributedPoint(r.nextDouble(), r.nextDouble(), i);
//        }
//    	DistributedLineSegment segment = new DistributedLineSegment(points);
//    	byte[] serializedsegment = segment.serialize();
//    	try {
//    		segment = DistributedLineSegment.deserialize(serializedsegment);	
//		} catch (Exception e) {
//			System.err.println("could not deserialize points");
//		}
//    	assertEquals(segment.points.length, 10);
//    	assertNotNull(segment.points[0].X);
//    	assertNotNull(segment.points[9].Y);
//    	System.out.println(segment.toString());
//    }
}
