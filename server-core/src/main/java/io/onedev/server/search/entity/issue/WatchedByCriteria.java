package io.onedev.server.search.entity.issue;

import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueWatch;
import io.onedev.server.model.User;
import io.onedev.server.util.criteria.Criteria;

import javax.persistence.criteria.*;

public class WatchedByCriteria extends Criteria<Issue> {

	private static final long serialVersionUID = 1L;

	private final User user;
	
	public WatchedByCriteria(User user) {
		this.user = user;
	}
	
	@Override
	public Predicate getPredicate(CriteriaQuery<?> query, From<Issue, Issue> from, CriteriaBuilder builder) {
		Subquery<IssueWatch> watchQuery = query.subquery(IssueWatch.class);
		Root<IssueWatch> watch = watchQuery.from(IssueWatch.class);
		watchQuery.select(watch);
		watchQuery.where(builder.and(
				builder.equal(watch.get(IssueWatch.PROP_ISSUE), from),
				builder.equal(watch.get(IssueWatch.PROP_USER), user)),
				builder.equal(watch.get(IssueWatch.PROP_WATCHING), true));
		return builder.exists(watchQuery);
	}

	@Override
	public boolean matches(Issue issue) {
		return issue.getWatches().stream().anyMatch(it -> it.isWatching() && it.getUser().equals(user));
	}

	@Override
	public String toStringWithoutParens() {
		return IssueQuery.getRuleName(IssueQueryLexer.WatchedBy) + " " + quote(user.getName());
	}

}
