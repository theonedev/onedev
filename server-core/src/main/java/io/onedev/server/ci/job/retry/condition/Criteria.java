package io.onedev.server.ci.job.retry.condition;

import java.io.Serializable;

import io.onedev.server.model.Build;

public abstract class Criteria implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	public abstract boolean satisfied(Build build);
	
}
