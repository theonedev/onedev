package io.onedev.server.buildspec.job;

import javax.annotation.Nullable;

import io.onedev.server.model.PullRequest;

public interface SubmitReason {

	@Nullable 
	String getRefName();
	
	PullRequest getPullRequest();
	
	String getDescription();
	
}
