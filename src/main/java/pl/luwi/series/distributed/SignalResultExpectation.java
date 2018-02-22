package pl.luwi.series.distributed;

import java.io.Serializable;

public class SignalResultExpectation implements Serializable {
	public Integer calculationIdentifier;
	public int lineID;
	
	public SignalResultExpectation(Integer calculationIdentifier, int lineID) {
		this.calculationIdentifier = calculationIdentifier;
		this.lineID = lineID;
	}
}
