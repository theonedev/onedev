package com.pmease.commons.util.concurrent;

public interface Prioritized extends Comparable<Prioritized> {
	int getPriority();
}
