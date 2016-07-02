package com.pmease.commons.util.concurrent;

public interface PriorityAware extends Comparable<PriorityAware> {
	int getPriority();
}
