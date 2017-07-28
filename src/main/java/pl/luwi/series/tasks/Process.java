package pl.luwi.series.tasks;

public abstract class Process implements Runnable {
	protected boolean isDone = false;
	
	private Process master;
	
	protected boolean isDone() {
		return isDone && master.isDone;
	};
	
	public Process(Process master) {
		this.master = master;
		isDone = false;
	}
	
	boolean isMasterDone() {
		return master.isDone();
	}
	
	public Process() {
		this.master = this;
	}
}
