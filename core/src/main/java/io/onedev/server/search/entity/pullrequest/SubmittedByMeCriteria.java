package io.onedev.server.search.entity.pullrequest;

import java.util.Objects;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.User;
import io.onedev.server.util.PullRequestConstants;

public class SubmittedByMeCriteria extends PullRequestCriteria {

	private static final long serialVersionUID = 1L;

	@Override
	public Predicate getPredicate(Project project, Root<PullRequest> root, CriteriaBuilder builder, User user) {
		Path<?> attribute = root.get(PullRequestConstants.ATTR_SUBMITTER);
		return builder.equal(attribute, user);
	}

	@Override
	public boolean matches(PullRequest request, User user) {
		return Objects.equals(request.getSubmitter(), user);
	}

	@Override
	public boolean needsLogin() {
		return true;
	}

	@Override
	public String toString() {
		return PullRequestQuery.getRuleName(PullRequestQueryLexer.SubmittedByMe);
	}

}
