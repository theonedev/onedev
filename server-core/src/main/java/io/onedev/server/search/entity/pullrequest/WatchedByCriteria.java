package io.onedev.server.search.entity.pullrequest;

import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestWatch;
import io.onedev.server.model.User;
import io.onedev.server.util.criteria.Criteria;

import javax.persistence.criteria.*;

public class WatchedByCriteria extends Criteria<PullRequest> {

	private static final long serialVersionUID = 1L;

	private final User user;
	
	public WatchedByCriteria(User user) {
		this.user = user;
	}
	
	@Override
	public Predicate getPredicate(CriteriaQuery<?> query, From<PullRequest, PullRequest> from, CriteriaBuilder builder) {
		Subquery<PullRequestWatch> watchQuery = query.subquery(PullRequestWatch.class);
		Root<PullRequestWatch> watch = watchQuery.from(PullRequestWatch.class);
		watchQuery.select(watch);
		watchQuery.where(builder.and(
				builder.equal(watch.get(PullRequestWatch.PROP_REQUEST), from),
				builder.equal(watch.get(PullRequestWatch.PROP_USER), user)),
				builder.equal(watch.get(PullRequestWatch.PROP_WATCHING), true));
		return builder.exists(watchQuery);
	}

	@Override
	public boolean matches(PullRequest request) {
		return request.getWatches().stream().anyMatch(it -> it.isWatching() && it.getUser().equals(user));
	}

	@Override
	public String toStringWithoutParens() {
		return PullRequestQuery.getRuleName(PullRequestQueryLexer.WatchedBy) + " " + quote(user.getName());
	}

}
