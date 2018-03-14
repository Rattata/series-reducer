package pl.luwi.series.distributed;

import java.io.Serializable;
import java.util.List;

public class LineResult implements Serializable {
	int RPDID ;
	int lineID;
	List<OrderedPoint> results;
	
	public LineResult(int RDPID, int lineID, List<OrderedPoint> results) {
		this.RPDID = RDPID;
		this.lineID = lineID;
		this.results = results;
	}
}
