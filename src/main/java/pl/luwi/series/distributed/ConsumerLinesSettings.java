package pl.luwi.series.distributed;

import java.io.Serializable;

public class ConsumerLinesSettings implements Serializable {
	int id = 0;
	public int COMPUTATION_THRESHOLD_SIZESPLIT = Constants.COMPUTATION_THRESHOLD_SIZESPLIT;
	public double COMPUTATION_THRESHOLD_SIZESEARCH = Constants.COMPUTATION_THRESHOLD_SIZESEARCH;
	public int COMPUTATION_SEARCHERS = Constants.COMPUTATION_SEARCHERS;
	public int COMPUTATION_CONSUMERS = Constants.COMPUTATION_CONSUMERS;
	public int COMPUTATION_SEARCH_PARTS = Constants.COMPUTATION_SEARCHERS;
	
}
