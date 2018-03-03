package pl.luwi.series.distributed;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IUpdatableNode extends Remote {
	public void update(ConsumerLinesSettings update) throws RemoteException;

	public void stop() throws RemoteException, Exception;

	public void start() throws RemoteException, Exception;
}
