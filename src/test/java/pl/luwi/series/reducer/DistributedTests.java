package pl.luwi.series.reducer;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static pl.luwi.series.reducer.Stopwatch.*;

import java.io.BufferedWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;

import javax.jms.JMSException;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

import org.testng.annotations.Test;

import pl.luwi.series.distributed.IndexedPoint;
import pl.luwi.series.distributed.OrderedPoint;
import pl.luwi.series.distributed.ConsumerLinesSettings;
import pl.luwi.series.distributed.ProcessingRegistrar;
import pl.luwi.series.distributed.ProcessingNode;
import pl.luwi.series.distributed.RegistrationService;
import pl.luwi.series.distributed.IUpdatableNode;

public class DistributedTests {
	@Test
	public void SimpleRun() {
		try {
			RegistrationService resv = ProcessingRegistrar.connect();
			List<IndexedPoint> n = create(40000);
			ConsumerLinesSettings settings = new ConsumerLinesSettings();
			settings.COMPUTATION_CONSUMERS = 2;
			settings.COMPUTATION_SEARCHERS = 2;
			settings.COMPUTATION_SEARCH_PARTS = 3;
			settings.COMPUTATION_THRESHOLD_SIZESPLIT = 500;

			IUpdatableNode node1 = ProcessingNode.connect("192.168.1.39");
//			 UpdatableNode node2 = ProcessingNode.connect("192.168.1.250");
			 node1.update(settings);
//			 node2.update(settings);
			 
			System.out.printf("%d -> %d\n",n.size(),resv.reduce(n, 0.00001).size() );
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
	List<Integer> N = Arrays.asList(new Integer[] { 10000, 40000, 160000 });
	List<Integer> split_thresholds = Arrays.asList(new Integer[] {500, 2000, 8000});
	List<Triplet<Integer,Integer,Integer>> lineSearchRatio = new ArrayList<DistributedTests.Triplet<Integer,Integer,Integer>>();
	
	@Test
	public void HappyFlow() {
		try {

			RegistrationService resv = ProcessingRegistrar.connect();
			IUpdatableNode node1 = ProcessingNode.connect("192.168.1.39");
			 IUpdatableNode node2 = ProcessingNode.connect("192.168.1.250");

			LocalDateTime now = LocalDateTime.now();
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd-mmss");

			String reportlocation = "./results-" + now.format(formatter) + ".csv";
			System.out.println(Paths.get(reportlocation).toAbsolutePath());
			BufferedWriter writer = Files.newBufferedWriter(Paths.get(reportlocation));

			CSVPrinter csvPrinter = new CSVPrinter(writer,
					CSVFormat.DEFAULT.withHeader("split", "n",  "line_consumers", "line_searchers", "search_segments", "time_taken"));

			int samples = 20;
			ConsumerLinesSettings settings = null;
			for (Triplet< Integer, Integer, Triplet<Integer, Integer,Integer>> triple : testSetup()) {
				// search, split, n, {consumer, searcher}
				settings = new ConsumerLinesSettings();
				settings.COMPUTATION_THRESHOLD_SIZESPLIT = triple.first;
				settings.COMPUTATION_CONSUMERS = triple.third.first;
				settings.COMPUTATION_SEARCHERS = triple.third.second;
				settings.COMPUTATION_SEARCH_PARTS = triple.third.third;

				node1.update(settings);
				node2.update(settings);
				List<IndexedPoint> n = create(triple.second);
				Thread.sleep(250);

				for (int i = 0; i < samples; i++) {
					Start();
					List<IndexedPoint> result = resv.reduce(n, 0.01);
					long time = Stop();
					csvPrinter.printRecord( triple.first, triple.second,triple.third.first, triple.third.second, triple.third.third, time);
					System.out.printf("%d %d %d %d %d %d \n",  triple.first, triple.second,triple.third.first, triple.third.second,triple.third.third, time);
					csvPrinter.flush();
				}

			}
			csvPrinter.flush();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// @Test
	// public void Connect() {
	// try {
	//// RegistrationService resv = ProcessRegistrar.connect();
	// UpdatableNode node = ProcessingNode.connect("192.168.1.250");
	// node.update(new ProcessLineSettings());
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// }

	private List<IndexedPoint> create(int numberOfPoints) {
		// (30x - x^2) * cos(2pi/ 0.5 * x) - 30
		List<IndexedPoint> points = new ArrayList<IndexedPoint>();
		double delta = 30 / (double) numberOfPoints;
		for (int j = 0; j < numberOfPoints; j++) {
			double i = j * delta;
			double y = (30 * i - Math.pow(i, 2)) * Math.cos((2 * Math.PI) * i) - 30;
			points.add(new IndexedPoint(j, y, j));
		}
		return points;
	}

	public class Tuple<U, V> {
		private final U first;
		private final V second;

		public Tuple(U first, V second) {
			this.first = first;
			this.second = second;
		}

		public V getSecond() {
			return second;
		}

		public U getFirst() {
			return first;
		}
	}

	public class Quint<T, U, V, Y> {

		private final T first;
		private final U second;
		private final V third;
		private final Y fourth;

		public Quint(T first, U second, V third, Y fourth) {
			this.first = first;
			this.second = second;
			this.third = third;
			this.fourth = fourth;
		}

		public T getFirst() {
			return first;
		}

		public U getSecond() {
			return second;
		}

		public V getThird() {
			return third;
		}

		public Y getFourth() {
			return fourth;
		}
	}

	public class Triplet<T, U, V> {

		private final T first;
		private final U second;
		private final V third;

		public Triplet(T first, U second, V third) {
			this.first = first;
			this.second = second;
			this.third = third;
		}

		public T getFirst() {
			return first;
		}

		public U getSecond() {
			return second;
		}

		public V getThird() {
			return third;
		}
	}

	

	private List<Triplet<Integer, Integer,Integer>> createConsumerSets() {
		lineSearchRatio.add(new Triplet<Integer,Integer, Integer>(2, 2,4));
		lineSearchRatio.add(new Triplet<Integer,Integer, Integer>(1, 3,4));
		return lineSearchRatio;
	}

	private List<Triplet< Integer, Integer, Triplet<Integer, Integer,Integer>>> testSetup() {
		List<Triplet< Integer, Integer, Triplet<Integer, Integer,Integer>>> cartesian = new ArrayList<>();
		List<Triplet<Integer, Integer,Integer>> consumerset = createConsumerSets();
		
			for (Integer split : split_thresholds) {
				for (Integer n : N) {
					for (Triplet<Integer, Integer,Integer> tuple : consumerset) {
						cartesian.add(
								new Triplet< Integer, Integer, Triplet<Integer, Integer,Integer>>(split, n, tuple));
					}
				}
			}
		
		return cartesian;
	}

	// @Test
	// public void SerializationTests() {
	// Random r = new Random();
	// int noPoints = 10;
	// DistributedPoint[] points= new DistributedPoint[noPoints];
	//
	// for(int i = 0 ; i < noPoints; i++){
	// points[i] = new DistributedPoint(r.nextDouble(), r.nextDouble(), i);
	// }
	// DistributedLineSegment segment = new DistributedLineSegment(points);
	// byte[] serializedsegment = segment.serialize();
	// try {
	// segment = DistributedLineSegment.deserialize(serializedsegment);
	// } catch (Exception e) {
	// System.err.println("could not deserialize points");
	// }
	// assertEquals(segment.points.length, 10);
	// assertNotNull(segment.points[0].X);
	// assertNotNull(segment.points[9].Y);
	// System.out.println(segment.toString());
	// }
}
