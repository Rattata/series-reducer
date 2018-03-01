package pl.luwi.series.sane;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface UpdatableNode extends Remote {
	public void update(ProcessLineSettings update) throws RemoteException;

	public void stop() throws RemoteException, Exception;

	public void start() throws RemoteException, Exception;
}
