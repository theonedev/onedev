package io.onedev.server.ee.xsearch;

import io.onedev.commons.utils.ExplicitException;

public class NoSubscriptionException extends ExplicitException {
	
	public NoSubscriptionException() {
		super("This feature requires an active subscription");
	}
	
}
