package pl.luwi.series.distributed;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

import pl.luwi.series.reducer.Point;

public interface RegistrationService  extends Remote  {
	int[] getSpreadIDs(int number)throws RemoteException;
	List<Integer> getLineIDs(int number)throws RemoteException;
	int getCalculationID() throws RemoteException;
	<P extends Point> List<P> reduce(List<P> points, double epsilon) throws RemoteException;
	void putResult(List<Integer> indices, Integer calculationID) throws RemoteException;
}
