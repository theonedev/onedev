package io.onedev.server.util.query;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nullable;

import io.onedev.server.model.AbstractEntity;
import io.onedev.server.model.User;
import io.onedev.server.model.support.NamedQuery;
import io.onedev.server.model.support.QuerySetting;

public abstract class QueryWatchBuilder<T extends AbstractEntity> {
	
	private final Map<String, Optional<EntityQuery<T>>> parsedQueries = new HashMap<>();
	
	private final Map<User, Boolean> watches = new HashMap<>();

	public QueryWatchBuilder() {
		for (QuerySetting<?> querySetting: getQuerySettings()) {
			boolean watched = false;
			for (Map.Entry<String, Boolean> entry: querySetting.getUserQueryWatches().entrySet()) {
				if (matches(querySetting.getUserQuery(entry.getKey()))) {
					watches.putIfAbsent(querySetting.getUser(), entry.getValue());
					watched = true;
					break;
				}
			}
			if (!watched) {
				for (Map.Entry<String, Boolean> entry: querySetting.getProjectQueryWatches().entrySet()) {
					if (matches(getSavedProjectQuery(entry.getKey()))) {
						watches.putIfAbsent(querySetting.getUser(), entry.getValue());
						watched = true;
						break;
					}
				}
			}
		}
	}
	
	private boolean matches(@Nullable NamedQuery namedQuery) {
		if (namedQuery != null) {
			Optional<EntityQuery<T>> entityQuery = parsedQueries.get(namedQuery.getQuery());
			if (entityQuery == null) {
				try {
					entityQuery = Optional.of(parse(namedQuery.getQuery()));
				} catch (Exception e) {
					entityQuery = Optional.empty();
				}
				parsedQueries.put(namedQuery.getQuery(), entityQuery);
			}
			return entityQuery.isPresent() && entityQuery.get().matches(getEntity()); 
		} else {
			return false;
		}
	}
	
	protected abstract T getEntity();
	
	protected abstract Collection<? extends QuerySetting<?>> getQuerySettings();
	
	protected abstract EntityQuery<T> parse(String queryString);
	
	protected abstract NamedQuery getSavedProjectQuery(String name);

	public Map<User, Boolean> getWatches() {
		return watches;
	}
	
}
