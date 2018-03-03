distributed-series-reducer
==============

A fork of the java implementation of RDP algorithm by LukaszWiktor for reducing number of points in data series. 
Now also threaded and distributed as a proof-of-concept.

Thank you, LukaszWiktor.

### How it works?###

The algorithm removes points from the series while maintaining the shape of the curve. See an example below:

![example 1](https://raw.github.com/wiki/LukaszWiktor/series-reducer/examples/example1.png)

### How to use it?###


All you need to do is to invoke `SeriesReducer.reduce(points, epsilon)` where `points` is an ordered list of points and `epsilon` defines a margin within which points can be removed.

Data presented on the example charts were produced by the following code snippet:
```java
List<Point> points = new ArrayList<Point>();
for (double x = 0; x < 4; x += 0.05) {
    points.add(new MyPoint(x, Math.cos(x*x - 1)));
}
List<Point> reduced = SeriesReducer.reduce(points, 0.01);
```

### How to run it distributed?###

All of the distributed/concurrent stuff is contained in the `pl.luwi.distributed` package.

1. modify values in the Constants class to the location of your JMS broker and RMI Registrar 
2. Make sure JMS broker is running
2. Run ProcessRegistrar.main
3. Run ProcessingNode.main with the IP of your interface
4. Connect to the ProcessRegistrar through RMI and submit a line

i.e.
```
java -cp target/series-reducer-0.3.0-SNAPSHOT-shaded.jar pl.luwi.series.distributed.ProcessingRegistrar
java -cp target/series-reducer-0.3.0-SNAPSHOT-shaded.jar pl.luwi.series.distributed.ProcessingNode 192.168.1.1

```
