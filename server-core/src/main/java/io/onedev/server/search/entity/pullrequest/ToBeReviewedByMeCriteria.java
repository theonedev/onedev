package io.onedev.server.search.entity.pullrequest;

import static io.onedev.server.web.translation.Translation._T;

import org.jspecify.annotations.Nullable;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Predicate;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.User;
import io.onedev.server.util.ProjectScope;
import io.onedev.server.util.criteria.Criteria;

public class ToBeReviewedByMeCriteria extends Criteria<PullRequest> {

	private static final long serialVersionUID = 1L;

	@Override
	public Predicate getPredicate(@Nullable ProjectScope projectScope, CriteriaQuery<?> query, From<PullRequest, PullRequest> from, CriteriaBuilder builder) {
		var user = User.get();
		if (user != null) 
			return getCriteria(user).getPredicate(projectScope, query, from, builder);
		else 
			throw new ExplicitException(_T("Please login to perform this query"));
	}

	@Override
	public boolean matches(PullRequest request) {
		var user = User.get();
		if (user != null) 
			return getCriteria(user).matches(request);
		else 
			throw new ExplicitException(_T("Please login to perform this query"));
	}
	
	private Criteria<PullRequest> getCriteria(User user) {
		return new ToBeReviewedByUserCriteria(user);
	}

	@Override
	public String toStringWithoutParens() {
		return PullRequestQuery.getRuleName(PullRequestQueryLexer.ToBeReviewedByMe);
	}

}
