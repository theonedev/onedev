package io.onedev.server.exception;

import io.onedev.commons.utils.ExplicitException;

public class PullRequestReviewRejectedException extends ExplicitException {

	public PullRequestReviewRejectedException(String message) {
		super(message);		
	}

}