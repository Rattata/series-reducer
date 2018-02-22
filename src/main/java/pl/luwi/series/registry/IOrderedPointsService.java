package pl.luwi.series.registry;

import java.rmi.RemoteException;

import pl.luwi.series.reducer.PointSegment;

public interface IOrderedPointsService {
	PointSegment getSegment() throws RemoteException;
}
