package io.onedev.server.util;

import java.io.Serializable;

public class IssueTimes implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private final long estimatedTime;
	
	private final long spentTime;

	public IssueTimes(long estimatedTime, long spentTime) {
		this.estimatedTime = estimatedTime;
		this.spentTime = spentTime;
	}

	public long getEstimatedTime() {
		return estimatedTime;
	}

	public long getSpentTime() {
		return spentTime;
	}
}
