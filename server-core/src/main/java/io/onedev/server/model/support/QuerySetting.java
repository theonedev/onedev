package io.onedev.server.model.support;

import java.util.ArrayList;

import javax.annotation.Nullable;
import javax.persistence.MappedSuperclass;

import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.util.watch.QuerySubscriptionSupport;
import io.onedev.server.util.watch.QueryWatchSupport;

@MappedSuperclass
public interface QuerySetting<T extends NamedQuery> {

	@Nullable
	Project getProject();
	
	User getUser();
	
	ArrayList<T> getUserQueries();
	
	void setUserQueries(ArrayList<T> userQueries);

	@Nullable
	QueryWatchSupport<T> getQueryWatchSupport();

	@Nullable
	QuerySubscriptionSupport<T> getQuerySubscriptionSupport();
	
}
