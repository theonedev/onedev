package io.onedev.server;

import io.onedev.server.annotation.NoDBAccess;
import org.apache.wicket.Component;

import javax.annotation.Nullable;

public interface SubscriptionService {

	@NoDBAccess
	boolean isSubscriptionActive();
	
	@Nullable
	String getLicensee();

	Component renderSupportRequestLink(String componentId);
	
}
