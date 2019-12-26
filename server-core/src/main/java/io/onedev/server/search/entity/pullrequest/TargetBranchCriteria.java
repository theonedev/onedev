package io.onedev.server.search.entity.pullrequest;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import io.onedev.server.model.PullRequest;
import io.onedev.server.search.entity.EntityCriteria;
import io.onedev.server.util.match.WildcardUtils;
import io.onedev.server.util.query.PullRequestQueryConstants;

public class TargetBranchCriteria extends EntityCriteria<PullRequest> {

	private static final long serialVersionUID = 1L;

	private final String branch;
	
	public TargetBranchCriteria(String value) {
		this.branch = value;
	}

	@Override
	public Predicate getPredicate(Root<PullRequest> root, CriteriaBuilder builder) {
		Path<String> attribute = root.get(PullRequestQueryConstants.ATTR_TARGET_BRANCH);
		String normalized = branch.toLowerCase().replace("*", "%");
		return builder.like(builder.lower(attribute), normalized);
	}

	@Override
	public boolean matches(PullRequest request) {
		return WildcardUtils.matchString(branch.toLowerCase(), request.getTargetBranch().toLowerCase());
	}

	@Override
	public String asString() {
		return quote(PullRequestQueryConstants.FIELD_TARGET_BRANCH) + " " 
				+ PullRequestQuery.getRuleName(PullRequestQueryLexer.Is) + " " 
				+ quote(branch);
	}

}
