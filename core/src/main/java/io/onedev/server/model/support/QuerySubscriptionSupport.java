package io.onedev.server.model.support;

import java.util.LinkedHashSet;

public abstract class QuerySubscriptionSupport<T extends NamedQuery> {

	public abstract LinkedHashSet<String> getUserQuerySubscriptions();

	public abstract LinkedHashSet<String> getProjectQuerySubscriptions();
	
}
