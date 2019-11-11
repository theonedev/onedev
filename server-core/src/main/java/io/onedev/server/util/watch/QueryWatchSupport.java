package io.onedev.server.util.watch;

import java.util.LinkedHashMap;
import java.util.Map;

import io.onedev.server.model.support.NamedQuery;

public abstract class QueryWatchSupport<T extends NamedQuery> {

	public abstract LinkedHashMap<String, Boolean> getUserQueryWatches();

	public abstract LinkedHashMap<String, Boolean> getQueryWatches();

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
	
	public WatchStatus getWatchStatus(T namedQuery) {
		return getWatchStatus(getQueryWatches(), namedQuery.getName());
	}
	
	public WatchStatus getUserWatchStatus(T namedQuery) {
		return getWatchStatus(getUserQueryWatches(), namedQuery.getName());
	}

	public void setWatchStatus(T namedQuery, WatchStatus watchStatus) {
		setWatchStatus(getQueryWatches(), namedQuery.getName(), watchStatus);
	}
	
	public void setUserWatchStatus(T namedQuery, WatchStatus watchStatus) {
		setWatchStatus(getUserQueryWatches(), namedQuery.getName(), watchStatus);
	}
	
}
