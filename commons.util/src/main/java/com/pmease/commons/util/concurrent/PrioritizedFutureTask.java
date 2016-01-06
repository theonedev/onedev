package com.pmease.commons.util.concurrent;

import java.util.concurrent.FutureTask;

public class PrioritizedFutureTask<T> extends FutureTask<T> 
		implements Prioritized, Comparable<Prioritized> {

	private final int priority;
	
	public PrioritizedFutureTask(PrioritizedCallable<T> callable) {
		super(callable);
		this.priority = callable.getPriority();
	}
	
	public PrioritizedFutureTask(PrioritizedRunnable runnable, T value) {
		super(runnable, value);
		this.priority = runnable.getPriority();
	}
	
	@Override
	public int getPriority() {
		return priority;
	}

	@Override
	public int compareTo(Prioritized o) {
		return priority - o.getPriority();
	}

}
