package pl.luwi.series.reducer;

import java.awt.Desktop;
import java.io.File;
import java.io.FileWriter;
import java.io.IOError;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class Test {

	private static final int iterations = 64;
	private static final int retrials = 40;
	public static int[] threadSequence = new int[] { 1, 2, 3, 4, 5 };
	public static int[] sizes;

	public static void main(String[] args) {
		try {
			Test test = new Test();

			// threadsequence, sizes, iterations
			sizes = new int[iterations];
			for (int i = 0; i < sizes.length; i++) {
				sizes[i] = (int) 225000 * (i + 1);
			}

			Long[][][] results = new Long[sizes.length][][];

			for (int j = sizes.length - 1; j >= 0; j--) {
				System.gc();
				results[j] = new Long[threadSequence.length][];
				PointImpl[] testset = test.getTestArray(sizes[j]);

				for (int i = 0; i < threadSequence.length; i++) {

					results[j][i] = test.runTest(testset, threadSequence[i], retrials);
					
					Thread.sleep(1);

				}
			}

			writecsv(results);

		} catch (Exception e) {
			e.printStackTrace();
			// TODO: handle exception
		}
	}

	public static void writecsv(Long[][][] matrix) throws IOException {
		File file = File.createTempFile(UUID.randomUUID().toString(), ".csv");

		FileWriter writer = new FileWriter(file);

		for (int j = 0; j < matrix.length; j++) {
			for (int i = 0; i < matrix[j].length; i++) {
				for (Long longs : matrix[j][i]) {
					writer.append(String.valueOf(sizes[j]) + ",");
					writer.append(String.valueOf(threadSequence[i]) + ",");
					writer.append(String.valueOf(longs) + "\n");
				}
			}
		}
		writer.close();
		System.out.println(file.getAbsolutePath());
		Desktop dt = Desktop.getDesktop();
		dt.open(file);
	}

	/**
	 * 
	 * @return timings in ns
	 */
	public Long[] runTest(PointImpl[] testset, int threads, int interations) {
		try {
			ArrayList<Long> timingresults = new ArrayList<>();
			for (int i = 0; i < iterations; i++) {
				long timing;
				if (threads == 1) {
					timing = sequentialTest(testset);
				} else {
					timing = concurrentTest(testset, threads);
				}
				timingresults.add(timing);
			}
			Long[] results = new Long[timingresults.size()];
			timingresults.toArray(results);
			String values = String.join(",",
					timingresults.stream().map(x -> String.valueOf(x)).collect(Collectors.toList()));
			return results;
		} catch (Exception e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
			return null;
		}
	}

	public PointImpl[] getTestArray(int number) {
		PointImpl[] array = new PointImpl[number];
		Random r = new Random();
		for (int i = 0; i < array.length ; i++) {
			array[i] = new PointImpl(r.nextDouble(), r.nextDouble());
		}
		return array;
	};

	private class result {
		int currentBest = Integer.MIN_VALUE;
		int currentBestIndex;
	}

	/**
	 * 
	 * @author siege
	 *
	 */
	private class FindMaximumTask implements Runnable {
		int startIndex;
		int endIndex;
		PointImpl[] testset;
		int currentBest = Integer.MIN_VALUE;
		int currentBestIndex;

		public FindMaximumTask(PointImpl[] testset, int startIndex, int endIndex) {
			this.testset = testset;
			this.startIndex = startIndex;
			this.endIndex = endIndex;
		}

		@Override
		public void run() {
			// start count

			Line<PointImpl> line = new Line<PointImpl>(testset[startIndex], testset[endIndex]);
			double currentBest = Double.MIN_VALUE;
			int currentBestIndex;
			// start count
			for (int i = startIndex + 1; i <= endIndex - 1; i++) {
				if (i > currentBest) {
					currentBest = line.distance(testset[i]);
					currentBestIndex = i;
				}
			}
		}

		@Override
		public String toString() {
			return "[" + startIndex + "," + endIndex + "]";
		}

	}

	private long concurrentTest(PointImpl[] testset, int threads) throws InterruptedException {
		// System.out.println("Created threadpool with " + threads + "
		// threads");
		long startDt = System.nanoTime();

		ArrayList<FindMaximumTask> tasks = new ArrayList<>();
		ExecutorService executorService = Executors.newFixedThreadPool(threads);
		float delta = (float) testset.length / threads;
		// System.out.println("delta:"+delta);
		for (int i = 0; i < threads; i++) {
			int indexStart = (int) (0 + (i * delta));
			int endIndex = (int) (delta * (i + 1) - 1);
			if (endIndex + 1 == testset.length)
				endIndex++;
			FindMaximumTask task = new FindMaximumTask(testset, indexStart, endIndex);
			tasks.add(task);
			executorService.submit(task);
		}
		executorService.shutdown();
		executorService.awaitTermination(10, TimeUnit.SECONDS);
		long result = System.nanoTime() - startDt;
		return result;
	}

	
	private long sequentialTest(PointImpl[] testset) {
		long startDt = System.nanoTime();
		Line<PointImpl> line = new Line<PointImpl>(testset[0], testset[testset.length -1]);
		double currentBest = Double.MIN_VALUE;
		int currentBestIndex;
		// start count
		for (int i = 1; i < testset.length - 1; i++) {
			if (i > currentBest) {
				currentBest = line.distance(testset[i]);
				currentBestIndex = i;
			}
		}
		return System.nanoTime() - startDt;
	}
}
