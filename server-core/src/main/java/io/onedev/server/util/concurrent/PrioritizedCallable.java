package io.onedev.server.util.concurrent;

import java.util.concurrent.Callable;

public abstract class PrioritizedCallable<T> extends Prioritized implements Callable<T> {

	public PrioritizedCallable(int priority) {
		super(priority);
	}
	
}
