package com.pmease.commons.util.concurrent;

public abstract class PrioritizedRunnable implements Runnable, Comparable<Prioritized>, Prioritized {

	private final int priority;

	public PrioritizedRunnable(int priority) {
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
