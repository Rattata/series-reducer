package pl.luwi.series.distributed;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class CalculationResultTask implements Serializable{
	Integer calculationIdentifier;
	
	HashMap<Integer,Integer> expected;
	HashMap<Integer,OrderedPoint> results;
	
	public CalculationResultTask(Integer calculationIdentifier) {
		this.calculationIdentifier = calculationIdentifier;
		expected = new HashMap<>();
		results = new HashMap<>();
	}
	
	public void receiveResult(TaskResult line){
		results.put(line.start.order, line.start);
		results.put(line.end.order, line.end);
		expected.remove(line.lineID);
	}
	
	public void expectLine(int lineID){
		expected.put(lineID, lineID);
	}
	
	public List<OrderedPoint> getResults(){
		return results.entrySet().stream().map(x -> x.getValue()).collect(Collectors.toList());
	}
	
	public boolean isDone(){
		return expected.isEmpty() && ! results.isEmpty(); 
	}
	
			
}
