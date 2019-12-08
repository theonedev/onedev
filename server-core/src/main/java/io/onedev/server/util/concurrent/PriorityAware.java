package io.onedev.server.util.concurrent;

public interface PriorityAware extends Comparable<PriorityAware> {
	int getPriority();
}
