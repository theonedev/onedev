package io.onedev.server.web.component.subscriptionstatus;

import java.io.Serial;

import io.onedev.server.web.page.base.BaseDependentCssResourceReference;

public class SubscriptionStatusResourceReference extends BaseDependentCssResourceReference {

	@Serial
	private static final long serialVersionUID = 1L;

	public SubscriptionStatusResourceReference() {
		super(SubscriptionStatusResourceReference.class, "subscription-status.css");
	}
	
}
