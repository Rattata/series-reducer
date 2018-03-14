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
			List<IndexedPoint> n = create(1000000);
			ConsumerLinesSettings settings = new ConsumerLinesSettings();
			settings.COMPUTATION_CONSUMERS = 2;
			settings.COMPUTATION_SEARCHERS = 2;
			settings.COMPUTATION_SEARCH_PARTS = 3;
			settings.COMPUTATION_THRESHOLD_SIZESPLIT = 8000;

			IUpdatableNode node1 = ProcessingNode.connect("192.168.1.39");
			IUpdatableNode node2 = ProcessingNode.connect("192.168.1.250");
			node1.update(settings);
			node2.update(settings);
			Start();
			resv.reduce(n, 0.00001);
			System.out.printf("%d -> %d\n", n.size(), Stop() / 1000000);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	int increment = 1000;
	List<Integer> split_thresholds = Arrays.asList(new Integer[] { 16000, 64000, 128000 });
	List<Triplet<Integer, Integer, Integer>> lineSearchRatio = new ArrayList<DistributedTests.Triplet<Integer, Integer, Integer>>();

	@Test
	public void HappyFlow() {
		try {

			RegistrationService resv = ProcessingRegistrar.connect();
			IUpdatableNode node1 = ProcessingNode.connect("192.168.1.39");
			// IUpdatableNode node2 = ProcessingNode.connect("192.168.1.250");

			LocalDateTime now = LocalDateTime.now();
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd-mmss");

			String reportlocation = "./results-" + now.format(formatter) + ".csv";
			System.out.println(Paths.get(reportlocation).toAbsolutePath());
			BufferedWriter writer = Files.newBufferedWriter(Paths.get(reportlocation));

			CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader("nodes", "split", "n",
					"line_consumers", "line_searchers", "search_segments", "time_taken"));
			int nodes = 1;

			ConsumerLinesSettings settings = null;
			for (int i = 256000; i >= 128000; i -= 12000) {
				List<IndexedPoint> problem = create(i);
				for (Tuple<Integer, Triplet<Integer, Integer, Integer>> setup : newTestSetup()) {

					settings = new ConsumerLinesSettings();
					settings.COMPUTATION_THRESHOLD_SIZESPLIT = setup.first;
					settings.COMPUTATION_CONSUMERS = setup.second.first;
					settings.COMPUTATION_SEARCHERS = setup.second.second;
					settings.COMPUTATION_SEARCH_PARTS = setup.second.third;

					node1.update(settings);
					// node2.update(settings);
					// "nodes", "split", "n",
					// "line_consumers", "line_searchers", "search_segments",
					// "ns_time_taken"
					Start();
					List<IndexedPoint> result = resv.reduce(problem, 0.01);
					long time = Stop();
					csvPrinter.printRecord(nodes, setup.first, i, setup.second.first, setup.second.second,
							setup.second.third, time);

					System.out.printf(" %d %d %d %d %d %d %d \n", 2, setup.first, i, setup.second.first,
							setup.second.second, setup.second.third, time);
					csvPrinter.flush();
				}
			}
			csvPrinter.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private List<Triplet<Integer, Integer, Integer>> createConsumerSets() {
		lineSearchRatio.add(new Triplet<Integer, Integer, Integer>(2, 2, 3));
		// lineSearchRatio.add(new Triplet<Integer, Integer, Integer>(1, 3, 4));
		return lineSearchRatio;
	}

	// splitthreshold
	private List<Tuple<Integer, Triplet<Integer, Integer, Integer>>> newTestSetup() {
		List<Tuple<Integer, Triplet<Integer, Integer, Integer>>> values = new ArrayList<>();
		for (Triplet<Integer, Integer, Integer> consumer : createConsumerSets()) {
			for (Integer split : split_thresholds) {
				values.add(new Tuple<Integer, Triplet<Integer, Integer, Integer>>(split, consumer));
			}

		}
		return values;
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
