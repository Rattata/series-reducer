package pl.luwi.series.distributed;

import java.io.Serializable;
import java.util.Arrays;
import static pl.luwi.series.distributed.Constants.*;

public class TaskFindmaxSpread implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public Line line;
	
	public TaskFindmaxSpread(Line line) {
		this.line = line;
	}

}
