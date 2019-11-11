package io.onedev.server.util.watch;

import java.util.LinkedHashSet;

import io.onedev.server.model.support.NamedQuery;

public abstract class QuerySubscriptionSupport<T extends NamedQuery> {

	public abstract LinkedHashSet<String> getUserQuerySubscriptions();

	public abstract LinkedHashSet<String> getQuerySubscriptions();
	
}
