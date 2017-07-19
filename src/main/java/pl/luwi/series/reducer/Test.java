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
import java.util.stream.Collectors;

public class Test {

	private static final int iterations = 20;
	private static final int retrials = 30;

	public static void main(String[] args) {
		Test test = new Test();

		int[] threadSequence = new int[] { 1, 2, 3, 4, 5, 6, 7, 8,9,10,11,12 };
		Long[][] results = new Long[iterations * retrials][];
		for (int i = 0; i < iterations; i++) {
			int testSize = (int) Math.pow(2, i + 7);
			for(int j = 0 ; j < retrials; j++){
				results[i* retrials + j] = test.runTest(test.getTestArray(testSize), threadSequence);
				System.gc();
			}
		}

		String[] columns = new String[] { "size", "1", "2", "3", "4", "5", "6", "7", "8","9", "10", "11", "12" };
		try {
			writecsv(results, columns);	
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	
	public static void writecsv(Long[][] matrix, String[] columnnames) throws IOException{
		File file = File.createTempFile(UUID.randomUUID().toString(), ".csv");
		
		FileWriter writer = new FileWriter(file);
		writer.append(String.join(",", columnnames) + "\n");
		
		for (int j = 0; j < matrix.length; j++) {
			List<String> strings = Arrays.asList(matrix[j]).stream().map((x) -> String.valueOf(x)).collect(Collectors.toList());
			writer.append(String.join(",", strings) + "\n");
		}
		writer.close();
		System.out.println(file.getAbsolutePath());
		Desktop dt = Desktop.getDesktop();
//		dt.open(file);
	}

	/**
	 * 
	 * @return timings in ns
	 */
	public Long[] runTest(int[] testset, int[] threadsequence) {
		try {
			ArrayList<Long> timingresults = new ArrayList<>();
			timingresults.add((long) testset.length);
			for (int i : threadsequence) {
				long timing;
				if (i == 1) {
					timing = sequentialTest(testset);
				} else {
					timing = concurrentTest(testset, i);
				}
				timingresults.add(timing);
			}
			Long[] results = new Long[timingresults.size()];
			timingresults.toArray(results);
			return results;
		} catch (Exception e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
			return null;
		}
	}

	public int[] getTestArray(int number) {
		int[] array = new int[number];
		Random r = new Random();
		for (int i : array) {
			i = r.nextInt();
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
		int[] array;
		int currentBest = Integer.MIN_VALUE;
		int currentBestIndex;

		public FindMaximumTask(int[] array, int startIndex, int endIndex) {
			this.array = array;
			this.startIndex = startIndex;
			this.endIndex = endIndex;
		}

		@Override
		public void run() {
			// start count
			for (int i = startIndex; i <= endIndex; i++) {
				if (i > currentBest) {
					currentBest = array[i];
					currentBestIndex = i;
				}
			}
		}

		@Override
		public String toString() {
			return "[" + startIndex + "," + endIndex + "]";
		}

	}

	private long concurrentTest(int[] testset, int threads) throws InterruptedException {
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

	private long sequentialTest(int[] testset) {
		long startDt = System.nanoTime();
		int currentBest = Integer.MIN_VALUE;
		int currentBestIndex;
		// start count
		for (int i = 0; i < testset.length; i++) {
			if (i > currentBest) {
				currentBest = testset[i];
				currentBestIndex = i;
			}
		}
		return System.nanoTime() - startDt;
	}
}
