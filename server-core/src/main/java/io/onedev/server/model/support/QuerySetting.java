package io.onedev.server.model.support;

import java.util.ArrayList;

import javax.annotation.Nullable;
import javax.persistence.MappedSuperclass;

import io.onedev.server.model.AbstractEntity;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.util.watch.QuerySubscriptionSupport;
import io.onedev.server.util.watch.QueryWatchSupport;

@MappedSuperclass
public abstract class QuerySetting<T extends NamedQuery> extends AbstractEntity {

	private static final long serialVersionUID = 1L;

	public abstract Project getProject();
	
	public abstract User getUser();
	
	public abstract ArrayList<T> getUserQueries();
	
	public abstract void setUserQueries(ArrayList<T> userQueries);

	@Nullable
	public abstract QueryWatchSupport<T> getQueryWatchSupport();

	@Nullable
	public abstract QuerySubscriptionSupport<T> getQuerySubscriptionSupport();
	
	@Nullable
	public T getUserQuery(String name) {
		for (T namedQuery: getUserQueries()) {
			if (namedQuery.getName().equals(name))
				return namedQuery;
		}
		return null;
	}

}
