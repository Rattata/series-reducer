package pl.luwi.series.sane;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

import javax.jms.JMSException;

public interface RegistrationService extends Remote {
	<P extends OrderedPoint> List<P> reduce(List<P> points, double epsilon) throws InterruptedException, JMSException, RemoteException;
	List<Integer> getLineIDs() throws RemoteException;
	Double getEpsilon(Integer calculationID) throws RemoteException;
	public <P extends OrderedPoint> void  submitResult(int RDPid, int lineID, List<P> result) throws RemoteException;
	public <P extends OrderedPoint> void  submitExpectation(int RDPid, int lineID) throws RemoteException;
	
}
