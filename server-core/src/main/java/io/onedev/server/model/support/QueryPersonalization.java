package io.onedev.server.model.support;

import java.util.ArrayList;

import org.jspecify.annotations.Nullable;
import javax.persistence.MappedSuperclass;

import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.util.watch.QuerySubscriptionSupport;
import io.onedev.server.util.watch.QueryWatchSupport;

@MappedSuperclass
public interface QueryPersonalization<T extends NamedQuery> {

	@Nullable
	Project getProject();
	
	User getUser();
	
	ArrayList<T> getQueries();
	
	void setQueries(ArrayList<T> userQueries);

	@Nullable
	QueryWatchSupport<T> getQueryWatchSupport();

	@Nullable
	QuerySubscriptionSupport<T> getQuerySubscriptionSupport();
	
	void onUpdated();
	
}
