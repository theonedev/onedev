package io.onedev.server.search.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import io.onedev.server.model.Issue;
import io.onedev.server.model.User;
import io.onedev.server.model.support.NamedQuery;
import io.onedev.server.model.support.QueryPersonalization;
import io.onedev.server.model.support.issue.NamedIssueQuery;
import io.onedev.server.search.entity.issue.IssueQuery;

class QueryWatchBuilderTest {

	@Test
	void usesSavedQueryOrderInsteadOfWatchActivationOrder() {
		var user = newUser();
		user.setIssueQueries(new ArrayList<>(List.of(query("all"), query("assigned"))));
		user.getIssueQueryWatches().put("p:assigned", true);
		user.getIssueQueryWatches().put("p:all", false);
		assertEquals(false, watches(user, List.of()).get(user));
		user.setIssueQueries(new ArrayList<>(List.of(query("assigned"), query("all"))));
		assertEquals(true, watches(user, List.of()).get(user));
	}

	@Test
	void skipsDefaultQueriesAndShadowedCommonQueries() {
		var user = newUser();
		user.setIssueQueries(new ArrayList<>(List.of(query("shared"))));
		user.getIssueQueryWatches().put("g:shared", false);
		user.getIssueQueryWatches().put("g:fallback", true);
		assertEquals(true, watches(user, List.of(query("shared"), query("fallback"))).get(user));
	}

	@Test
	void usesCommonQueryOrderAndIgnoresStaleWatchNames() {
		var user = newUser();
		user.setIssueQueryWatches(new LinkedHashMap<>());
		user.getIssueQueryWatches().put("p:removed", true);
		user.getIssueQueryWatches().put("g:second", true);
		user.getIssueQueryWatches().put("g:first", false);
		assertEquals(false, watches(user, List.of(query("first"), query("second"))).get(user));
	}

	private User newUser() {
		var user = new User();
		user.setId(1L);
		return user;
	}

	private NamedIssueQuery query(String name) {
		return new NamedIssueQuery(name, "");
	}

	private Map<User, Boolean> watches(User user, List<NamedIssueQuery> commonQueries) {
		var issue = new Issue();
		return new QueryWatchBuilder<Issue>() {
			@Override
			protected Issue getEntity() {
				return issue;
			}

			@Override
			protected Collection<? extends QueryPersonalization<?>> getQueryPersonalizations() {
				return List.of(user.getIssueQueryPersonalization());
			}

			@Override
			protected EntityQuery<Issue> parse(String queryString) {
				return new IssueQuery();
			}

			@Override
			protected Collection<? extends NamedQuery> getNamedQueries() {
				return commonQueries;
			}
		}.getWatches();
	}
}
