package io.onedev.server.search.entity.pullrequest;

import static io.onedev.server.search.entity.pullrequest.PullRequestQueryLexer.IgnoredBy;

import org.jspecify.annotations.Nullable;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestWatch;
import io.onedev.server.model.User;
import io.onedev.server.util.ProjectScope;
import io.onedev.server.util.criteria.Criteria;

public class IgnoredByUserCriteria extends Criteria<PullRequest> {

	private static final long serialVersionUID = 1L;

	private final User user;
	
	public IgnoredByUserCriteria(User user) {
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
				builder.equal(watch.get(PullRequestWatch.PROP_WATCHING), false));
		return builder.exists(watchQuery);
	}

	@Override
	public boolean matches(PullRequest request) {
		return request.getWatches().stream().anyMatch(it -> !it.isWatching() && it.getUser().equals(user));
	}

	@Override
	public String toStringWithoutParens() {
		return PullRequestQuery.getRuleName(IgnoredBy) + " " + quote(user.getName());
	}

} 