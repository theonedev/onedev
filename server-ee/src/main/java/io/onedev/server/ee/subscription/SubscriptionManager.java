package io.onedev.server.ee.subscription;

import javax.annotation.Nullable;

public interface SubscriptionManager {
	
	boolean isActive();
	
	@Nullable
	String getLicensee();
	
}
