package pl.luwi.series.distributed;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TaskOrderedLine<P extends OrderedPoint> implements Serializable {
    
	public Integer RDPID;
	public Integer lineID;
	
	public List<P> points;
	
    private P start;
    private P end;
    
    private double dx;
    private double dy;
    private double sxey;
    private double exsy;
    private  double length;
       
    public TaskOrderedLine(List<P> points, int RDPID, int lineID)
    {
    	this.RDPID = RDPID;
    	this.lineID = lineID;
    	this.start = points.get(0);
    	this.points = points;
    	this.end = points.get(points.size() - 1);
    	dx = start.getX() - end.getX();
        dy = start.getY() - end.getY();
        sxey = start.getX() * end.getY();
        exsy = end.getX() * start.getY();
        length = Math.sqrt(dx*dx + dy*dy);
    }
    
    private TaskOrderedLine(List<P> points, int RDPID){
    	this.RDPID = RDPID;
    	this.start = points.get(0);
    	this.points = points;
    	this.end = points.get(points.size() - 1);
    	dx = start.getX() - end.getX();
        dy = start.getY() - end.getY();
        sxey = start.getX() * end.getY();
        exsy = end.getX() * start.getY();
        length = Math.sqrt(dx*dx + dy*dy);
    }
    
   
    
    @SuppressWarnings("unchecked")
    public List<P> asList() {
        return Arrays.asList(start, end);
    }
    
    public double distance(OrderedPoint p) {
        return Math.abs(dy * p.getX() - dx * p.getY() + sxey - exsy) / length;
    }
    
    public P getStart() {
		return start;
	}
    
    public P getEnd() {
		return end;
	}
    
    public RDPresult<P> asResult(){
    	ArrayList<P> results = new ArrayList<>(2);
    	results.add(getStart());
    	results.add(getEnd());
    	return new RDPresult<P>(null, results);
    }
    
    public RDPresult<P> asSplit(int index){
    	List<TaskOrderedLine<P>> lines = new ArrayList<>();
    	List<P> a = new ArrayList<P>(points.subList(0, index + 1));
    	lines.add(new TaskOrderedLine<P>(a, this.RDPID));
    	List<P> b = new ArrayList<P>(points.subList(index, points.size()));
    	lines.add(new TaskOrderedLine<P>(b, this.RDPID));
    	return new RDPresult<P>(lines, null);
    }
    
    public class RDPresult<E extends OrderedPoint>{
		List<TaskOrderedLine<E>> lines;
		List<E> points;
		public RDPresult(List<TaskOrderedLine<E>> lines, List<E> points){
			this.lines = lines;
			this.points = points;
		}
	}
}


