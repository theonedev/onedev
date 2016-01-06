package com.pmease.commons.util.concurrent;

import java.util.concurrent.Callable;

public abstract class PrioritizedCallable<T> implements Callable<T>, Comparable<Prioritized>, Prioritized {

	private final int priority;

	public PrioritizedCallable(int priority) {
		this.priority = priority;
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
