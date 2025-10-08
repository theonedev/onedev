package io.onedev.server.search.entity.pullrequest;

import static io.onedev.server.web.translation.Translation._T;

import org.jspecify.annotations.Nullable;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.User;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.ProjectScope;

public class SubmittedByMeCriteria extends SubmittedByCriteria {

	private static final long serialVersionUID = 1L;

	@Override
	public Predicate getPredicate(@Nullable ProjectScope projectScope, CriteriaQuery<?> query, From<PullRequest, PullRequest> from, CriteriaBuilder builder) {
		if (User.get() != null) {
			Path<?> attribute = from.get(PullRequest.PROP_SUBMITTER);
			return builder.equal(attribute, User.get());
		} else {
			throw new ExplicitException(_T("Please login to perform this query"));
		}
	}

	@Override
	public User getUser() {
		return SecurityUtils.getUser();
	}

	@Override
	public boolean matches(PullRequest request) {
		if (User.get() != null)
			return User.get().equals(request.getSubmitter());
		else
			throw new ExplicitException(_T("Please login to perform this query"));
	}

	@Override
	public String toStringWithoutParens() {
		return PullRequestQuery.getRuleName(PullRequestQueryLexer.SubmittedByMe);
	}

}
