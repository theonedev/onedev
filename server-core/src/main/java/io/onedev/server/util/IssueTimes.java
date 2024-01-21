package io.onedev.server.util;

import javax.annotation.Nullable;
import java.io.Serializable;

public class IssueTimes implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private final long estimatedTime;
	
	private final long spentTime;

	public IssueTimes(@Nullable Long estimatedTime, @Nullable Long spentTime) {
		this.estimatedTime = estimatedTime != null? estimatedTime: 0;
		this.spentTime = spentTime != null? spentTime: 0;
	}

	public int getEstimatedTime() {
		return (int) estimatedTime;
	}

	public int getSpentTime() {
		return (int) spentTime;
	}
}
