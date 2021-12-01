package io.onedev.server.search.entity.pullrequest;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import io.onedev.server.model.PullRequest;
import io.onedev.server.search.entity.EntityCriteria;
import io.onedev.server.util.match.WildcardUtils;

public class TargetBranchCriteria extends EntityCriteria<PullRequest> {

	private static final long serialVersionUID = 1L;

	private final String branch;
	
	public TargetBranchCriteria(String value) {
		this.branch = value;
	}

	@Override
	public Predicate getPredicate(CriteriaQuery<?> query, From<PullRequest, PullRequest> from, CriteriaBuilder builder) {
		Path<String> attribute = from.get(PullRequest.PROP_TARGET_BRANCH);
		String normalized = branch.toLowerCase().replace("*", "%");
		return builder.like(builder.lower(attribute), normalized);
	}

	@Override
	public boolean matches(PullRequest request) {
		return WildcardUtils.matchString(branch.toLowerCase(), request.getTargetBranch().toLowerCase());
	}

	@Override
	public String toStringWithoutParens() {
		return quote(PullRequest.NAME_TARGET_BRANCH) + " " 
				+ PullRequestQuery.getRuleName(PullRequestQueryLexer.Is) + " " 
				+ quote(branch);
	}

}
