package io.onedev.server.buildspec.job;

import javax.annotation.Nullable;

import io.onedev.server.model.PullRequest;

public interface SubmitReason {

	String getRefName();
	
	@Nullable
	PullRequest getPullRequest();
	
	String getDescription();
	
}
