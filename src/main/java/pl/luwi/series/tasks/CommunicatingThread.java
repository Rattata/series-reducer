package pl.luwi.series.tasks;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import pl.luwi.series.tasks.CommunicatingThread.STATE;

import static pl.luwi.series.tasks.ConcurrentConstants.TIMEOUT_uS;

public abstract class CommunicatingThread<Input extends LinkedBlockingQueue<?>, Output extends LinkedBlockingQueue<?>> extends Thread {

	private List<CommunicatingThread<?,?>> peers = new ArrayList<>();
	protected Input inputQueue;
	protected Output outputQueue;
	private int commMisses = 0;
	
	protected int timeOut = TIMEOUT_uS;
	
	public CommunicatingThread(Input inputQueue, Output outputQueue) {
		this.inputQueue = inputQueue;
		this.outputQueue = outputQueue;
	}
	
	public void setPeers(List<CommunicatingThread<?, ?>> peers) {
		this.peers = peers;
	}
	
	public static enum STATE {
		RUNNING, WAITING, DEAD
	}

	private STATE currentState = STATE.RUNNING;

	public STATE getCurrentState() {
		return currentState;
	}
	
	public boolean ArePeersDone() {
		for (CommunicatingThread<?,?> communicatingThread : peers) {
			if (communicatingThread.currentState.equals(STATE.RUNNING)) {
				return false;
			}
		}
		return true;
	}

	public int getCommMisses() {
		return commMisses;
	}
	
	@Override
	public void run() {
		try {
			while (peers.stream().anyMatch(x -> ! x.IsDone()) ) {
				Running();
				process(inputQueue, outputQueue);
				Wait();
				yield();
				commMisses++;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		Dead();
	}

	public abstract void process(Input inputQueue, Output outputQueue) throws Exception;

	public void addPeer(CommunicatingThread<?,?> peer) {
		peers.add(peer);
	}

	private void Wait() {
		currentState = STATE.WAITING;
	}

	private void Dead() {
		currentState = STATE.DEAD;
	}

	private void Running() {
		currentState = STATE.RUNNING;
	}

	public synchronized boolean IsDone(){
		return ! this.getCurrentState().equals(STATE.RUNNING) && inputQueue.isEmpty() && outputQueue.isEmpty();
	};
}
