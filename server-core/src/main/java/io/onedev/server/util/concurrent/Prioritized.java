package io.onedev.server.util.concurrent;

public class Prioritized implements PriorityAware {

	private final int priority;

	public Prioritized(int priority) {
		this.priority = priority;
	}
	
	@Override
	public int getPriority() {
		return priority;
	}

	@Override
	public int compareTo(PriorityAware o) {
		return priority - o.getPriority();
	}
	
}
