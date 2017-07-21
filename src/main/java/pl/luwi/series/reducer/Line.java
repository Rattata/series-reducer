package pl.luwi.series.reducer;

import java.util.Arrays;
import java.util.List;

public class Line<P extends Point> {
    
    private P start;
    private P end;
    
    private double dx;
    private double dy;
    private double sxey;
    private double exsy;
    private  double length;
    
    public Line(P start, P end) {
        this.start = start;
        this.end = end;
        dx = start.getX() - end.getX();
        dy = start.getY() - end.getY();
        sxey = start.getX() * end.getY();
        exsy = end.getX() * start.getY();
        length = Math.sqrt(dx*dx + dy*dy);
    }
    
    public Line(Line<P> l) {
    	this.start = l.start;
        this.end = l.end;
        this.dx = l.dx;
        this.dy = l.dy;
        this.sxey = l.sxey;
        this.exsy = l.exsy;
        this.length = l.length;
    }
    
    @SuppressWarnings("unchecked")
    public List<P> asList() {
        return Arrays.asList(start, end);
    }
    
    public double distance(Point p) {
        return Math.abs(dy * p.getX() - dx * p.getY() + sxey - exsy) / length;
    }
}


