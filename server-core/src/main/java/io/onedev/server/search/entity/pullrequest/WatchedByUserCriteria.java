package io.onedev.server.search.entity.pullrequest;

import static io.onedev.server.search.entity.pullrequest.PullRequestQueryLexer.WatchedBy;

import org.jspecify.annotations.Nullable;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;

import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestWatch;
import io.onedev.server.model.User;
import io.onedev.server.util.ProjectScope;
import io.onedev.server.util.criteria.Criteria;

public class WatchedByUserCriteria extends Criteria<PullRequest> {

	private static final long serialVersionUID = 1L;

	private final User user;
	
	public WatchedByUserCriteria(User user) {
		this.user = user;
	}
	
	@Override
	public Predicate getPredicate(@Nullable ProjectScope projectScope, CriteriaQuery<?> query, From<PullRequest, PullRequest> from, CriteriaBuilder builder) {
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
		return PullRequestQuery.getRuleName(WatchedBy) + " " + quote(user.getName());
	}

}
