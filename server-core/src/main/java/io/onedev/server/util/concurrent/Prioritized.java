package io.onedev.server.util.concurrent;

public class Prioritized implements Comparable<Prioritized> {

	private final int priority;

	public Prioritized(int priority) {
		this.priority = priority;
	}
	
	public int getPriority() {
		return priority;
	}

	@Override
	public int compareTo(Prioritized o) {
		return priority - o.getPriority();
	}
	
}
