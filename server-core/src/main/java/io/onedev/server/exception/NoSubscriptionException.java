package io.onedev.server.exception;

import io.onedev.commons.utils.ExplicitException;

public class NoSubscriptionException extends ExplicitException {
	
	public NoSubscriptionException(String feature) {
		super(feature + " requires an active subscription");
	}
	
}
