package com.gitplex.commons.util.concurrent;

public interface PriorityAware extends Comparable<PriorityAware> {
	int getPriority();
}
