package io.onedev.server.util.reviewrequirement;

import io.onedev.server.util.OneException;

public class InvalidReviewRuleException extends OneException {

	private static final long serialVersionUID = 1L;

	public InvalidReviewRuleException(String errorMessage) {
		super(errorMessage);
	}
	
}
