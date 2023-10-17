package io.onedev.server;

import org.apache.wicket.Component;

import javax.annotation.Nullable;

public interface SubscriptionManager {
	
	boolean isSubscriptionActive();
	
	@Nullable
	String getLicensee();

	Component renderSupportRequestLink(String componentId);
	
}
