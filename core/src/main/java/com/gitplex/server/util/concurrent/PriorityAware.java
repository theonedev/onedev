package com.gitplex.server.util.concurrent;

public interface PriorityAware extends Comparable<PriorityAware> {
	int getPriority();
}
