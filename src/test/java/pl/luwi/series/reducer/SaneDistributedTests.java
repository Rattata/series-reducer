package pl.luwi.series.reducer;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static pl.luwi.series.reducer.Stopwatch.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.jms.JMSException;

import org.testng.annotations.Test;

import pl.luwi.series.sane.IndexedPoint;
import pl.luwi.series.sane.OrderedPoint;
import pl.luwi.series.sane.ProcessRegistrar;
import pl.luwi.series.sane.RegistrationService;


public class SaneDistributedTests {
    
	@Test
	public void HappyFlow(){
ArrayList<IndexedPoint> points = new ArrayList<>();
		Random r = new Random();
		for (int i = 0; i <= 2000000; i++) {
			points.add(new IndexedPoint(r.nextDouble()* 100, r.nextDouble() * 100, i));
		}
		try {
			RegistrationService resv =  ProcessRegistrar.connect();
			Start();
			List<IndexedPoint>  result=  resv.reduce(points, 20);
			System.out.println(Stop() / 1000000000);
			System.out.println(result.size());
		} catch (Exception e) {
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
