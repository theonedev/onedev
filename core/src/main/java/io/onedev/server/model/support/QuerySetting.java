package io.onedev.server.model.support;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.annotation.Nullable;
import javax.persistence.MappedSuperclass;

import io.onedev.server.model.AbstractEntity;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;

@MappedSuperclass
public abstract class QuerySetting<T extends NamedQuery> extends AbstractEntity {

	private static final long serialVersionUID = 1L;

	public abstract Project getProject();
	
	public abstract User getUser();
	
	public abstract ArrayList<T> getUserQueries();
	
	public abstract void setUserQueries(ArrayList<T> userQueries);

	public abstract LinkedHashMap<String, Boolean> getUserQueryWatches();

	public abstract LinkedHashMap<String, Boolean> getProjectQueryWatches();

	private WatchStatus getWatchStatus(Map<String, Boolean> watches, String name) {
		Boolean watching = watches.get(name);
		if (Boolean.TRUE.equals(watching))
			return WatchStatus.WATCH;
		else if (Boolean.FALSE.equals(watching))
			return WatchStatus.DO_NOT_WATCH;
		else
			return WatchStatus.DEFAULT;
	}
	
	private void setWatchStatus(Map<String, Boolean> watches, String name, WatchStatus watchStatus) {
		if (watchStatus != WatchStatus.DEFAULT) 
			watches.put(name, watchStatus == WatchStatus.WATCH);
		else
			watches.remove(name);
	}
	
	public WatchStatus getProjectWatchStatus(T namedQuery) {
		return getWatchStatus(getProjectQueryWatches(), namedQuery.getName());
	}
	
	public WatchStatus getUserWatchStatus(T namedQuery) {
		return getWatchStatus(getUserQueryWatches(), namedQuery.getName());
	}

	public void setProjectWatchStatus(T namedQuery, WatchStatus watchStatus) {
		setWatchStatus(getProjectQueryWatches(), namedQuery.getName(), watchStatus);
	}
	
	public void setUserWatchStatus(T namedQuery, WatchStatus watchStatus) {
		setWatchStatus(getUserQueryWatches(), namedQuery.getName(), watchStatus);
	}

	@Nullable
	public T getUserQuery(String name) {
		for (T namedQuery: getUserQueries()) {
			if (namedQuery.getName().equals(name))
				return namedQuery;
		}
		return null;
	}

}
