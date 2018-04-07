package io.onedev.server.util.reviewrequirement;

import io.onedev.server.exception.OneException;

public class InvalidReviewRuleException extends OneException {

	private static final long serialVersionUID = 1L;

	public InvalidReviewRuleException(String errorMessage) {
		super(errorMessage);
	}
	
}
