package io.onedev.server.buildspec.job;

import javax.annotation.Nullable;

import io.onedev.server.model.PullRequest;

public interface SubmitReason {

	@Nullable 
	String getUpdatedRef();
	
	PullRequest getPullRequest();
	
	String getDescription();
	
}
