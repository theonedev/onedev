package io.onedev.server.search.entity.pullrequest;

import java.util.Objects;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import io.onedev.server.model.PullRequest;
import io.onedev.server.model.User;
import io.onedev.server.util.PullRequestConstants;

public class TargetBranchCriteria extends PullRequestCriteria {

	private static final long serialVersionUID = 1L;

	private final String branch;
	
	public TargetBranchCriteria(String value) {
		this.branch = value;
	}

	@Override
	public Predicate getPredicate(Root<PullRequest> root, CriteriaBuilder builder, User user) {
		Path<User> attribute = root.get(PullRequestConstants.ATTR_TARGET_BRANCH);
		return builder.equal(attribute, branch);
	}

	@Override
	public boolean matches(PullRequest request, User user) {
		return Objects.equals(request.getTargetBranch(), branch);
	}

	@Override
	public boolean needsLogin() {
		return false;
	}

	@Override
	public String toString() {
		return PullRequestQuery.quote(PullRequestConstants.FIELD_TARGET_BRANCH) + " " 
				+ PullRequestQuery.getRuleName(PullRequestQueryLexer.Is) + " " 
				+ PullRequestQuery.quote(branch);
	}

}
