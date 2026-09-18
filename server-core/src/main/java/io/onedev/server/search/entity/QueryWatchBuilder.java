package io.onedev.server.search.entity;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import org.jspecify.annotations.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.onedev.server.model.AbstractEntity;
import io.onedev.server.model.Issue;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.User;
import io.onedev.server.model.support.NamedQuery;
import io.onedev.server.model.support.QueryPersonalization;

public abstract class QueryWatchBuilder<T extends AbstractEntity> {
	
	private static final Logger logger = LoggerFactory.getLogger(QueryWatchBuilder.class);
	
	private final Map<String, Optional<EntityQuery<T>>> parsedQueries = new HashMap<>();
	
	private final Map<User, Boolean> watches = new HashMap<>();

	public QueryWatchBuilder() {
		for (QueryPersonalization<?> personalization: getQueryPersonalizations()) {
			// Match the displayed order: personal queries, then unshadowed common queries.
			// The watch map's insertion order only records when watches were enabled.
			Map<String, NamedQuery> orderedQueries = new LinkedHashMap<>();
			for (var query: personalization.getQueries())
				orderedQueries.put(NamedQuery.PERSONAL_NAME_PREFIX + query.getName(), query);
			for (var query: getNamedQueries()) {
				if (NamedQuery.find(personalization.getQueries(), query.getName()) == null)
					orderedQueries.put(NamedQuery.COMMON_NAME_PREFIX + query.getName(), query);
			}
			var queryWatches = personalization.getQueryWatchSupport().getQueryWatches();
			for (var entry: orderedQueries.entrySet()) {
				var watching = queryWatches.get(entry.getKey());
				if (watching != null && matches(entry.getValue(), personalization.getUser())) {
					watches.putIfAbsent(personalization.getUser(), watching);
					break;
				}
			}
		}
	}
	
	private boolean matches(@Nullable NamedQuery namedQuery, User user) {
		if (namedQuery != null) {
			User.push(user);
			try {
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
			} catch (Exception e) {
				String message;
				if (getEntity() instanceof Issue) {
					Issue issue = (Issue) getEntity();
					message = String.format("Error processing watches (user: %s, issue: %s, query: %s)", 
							user.getName(), issue.getReference(), namedQuery.getQuery());
				} else if (getEntity() instanceof PullRequest) {
					PullRequest request = (PullRequest) getEntity();
					message = String.format("Error processing watches (user: %s, pull request: %s, query: %s)", 
							user.getName(), request.getReference().toString(null), namedQuery.getQuery());
				} else {
					throw new RuntimeException("Unexpected watch entity type: " + getEntity().getClass());
				}
				logger.error(message, e);
			} finally {
				User.pop();
			}
		} 
		return false;
	}
	
	protected abstract T getEntity();
	
	protected abstract Collection<? extends QueryPersonalization<?>> getQueryPersonalizations();
	
	protected abstract EntityQuery<T> parse(String queryString);
	
	protected abstract Collection<? extends NamedQuery> getNamedQueries();

	public Map<User, Boolean> getWatches() {
		return watches;
	}
	
}
