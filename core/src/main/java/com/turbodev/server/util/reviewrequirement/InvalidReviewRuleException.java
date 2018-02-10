package com.turbodev.server.util.reviewrequirement;

public class InvalidReviewRuleException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public InvalidReviewRuleException(String errorMessage) {
		super(errorMessage);
	}
	
}
