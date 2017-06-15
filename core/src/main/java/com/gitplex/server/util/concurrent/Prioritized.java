package com.gitplex.server.util.concurrent;

public class Prioritized implements PriorityAware {

	private final int priority;

	public Prioritized(int priority) {
		this.priority = priority;
	}
	
	@Override
	public int getPriority() {
		return priority;
	}

}
