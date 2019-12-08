package io.onedev.server.util.concurrent;

public abstract class PrioritizedRunnable extends Prioritized implements Runnable {

	public PrioritizedRunnable(int priority) {
		super(priority);
	}
	
}
