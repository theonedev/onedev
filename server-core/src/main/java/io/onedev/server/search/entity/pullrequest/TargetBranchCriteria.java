package io.onedev.server.search.entity.pullrequest;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import io.onedev.commons.utils.match.WildcardUtils;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.User;
import io.onedev.server.search.entity.EntityCriteria;
import io.onedev.server.util.query.PullRequestQueryConstants;

public class TargetBranchCriteria extends EntityCriteria<PullRequest> {

	private static final long serialVersionUID = 1L;

	private final String branch;
	
	public TargetBranchCriteria(String value) {
		this.branch = value;
	}

	@Override
	public Predicate getPredicate(Root<PullRequest> root, CriteriaBuilder builder, User user) {
		Path<String> attribute = root.get(PullRequestQueryConstants.ATTR_TARGET_BRANCH);
		String normalized = branch.toLowerCase().replace("*", "%");
		return builder.like(builder.lower(attribute), normalized);
	}

	@Override
	public boolean matches(PullRequest request, User user) {
		return WildcardUtils.matchString(branch.toLowerCase(), request.getTargetBranch().toLowerCase());
	}

	@Override
	public boolean needsLogin() {
		return false;
	}

	@Override
	public String toString() {
		return PullRequestQuery.quote(PullRequestQueryConstants.FIELD_TARGET_BRANCH) + " " 
				+ PullRequestQuery.getRuleName(PullRequestQueryLexer.Is) + " " 
				+ PullRequestQuery.quote(branch);
	}

}
