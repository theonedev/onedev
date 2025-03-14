package io.onedev.server.util.watch;

import java.util.LinkedHashMap;
import java.util.Map;

import io.onedev.server.model.support.NamedQuery;

public abstract class QueryWatchSupport<T extends NamedQuery> {

	public abstract LinkedHashMap<String, Boolean> getQueryWatches();

	private WatchStatus getWatchStatus(Map<String, Boolean> watches, String queryName) {
		Boolean watching = watches.get(queryName);
		if (Boolean.TRUE.equals(watching))
			return WatchStatus.WATCH;
		else if (Boolean.FALSE.equals(watching))
			return WatchStatus.IGNORE;
		else
			return WatchStatus.DEFAULT;
	}
	
	private void setWatchStatus(Map<String, Boolean> watches, String queryName, WatchStatus watchStatus) {
		if (watchStatus != WatchStatus.DEFAULT) 
			watches.put(queryName, watchStatus == WatchStatus.WATCH);
		else
			watches.remove(queryName);
	}
	
	public WatchStatus getWatchStatus(String queryName) {
		return getWatchStatus(getQueryWatches(), queryName);
	}
	
	public void setWatchStatus(String queryName, WatchStatus watchStatus) {
		setWatchStatus(getQueryWatches(), queryName, watchStatus);
	}
	
}
