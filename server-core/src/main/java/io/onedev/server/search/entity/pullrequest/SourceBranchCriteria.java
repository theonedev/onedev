package io.onedev.server.search.entity.pullrequest;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import io.onedev.server.model.PullRequest;
import io.onedev.server.util.criteria.Criteria;
import io.onedev.server.util.match.WildcardUtils;

public class SourceBranchCriteria extends Criteria<PullRequest> {

	private static final long serialVersionUID = 1L;

	private final String branch;
	
	public SourceBranchCriteria(String value) {
		this.branch = value;
	}

	@Override
	public Predicate getPredicate(CriteriaQuery<?> query, From<PullRequest, PullRequest> from, CriteriaBuilder builder) {
		Path<String> attribute = from.get(PullRequest.PROP_SOURCE_BRANCH);
		String normalized = branch.toLowerCase().replace("*", "%");
		return builder.like(builder.lower(attribute), normalized);
	}

	@Override
	public boolean matches(PullRequest request) {
		return WildcardUtils.matchString(branch.toLowerCase(), request.getSourceBranch().toLowerCase());
	}

	@Override
	public String toStringWithoutParens() {
		return quote(PullRequest.NAME_SOURCE_BRANCH) + " " 
				+ PullRequestQuery.getRuleName(PullRequestQueryLexer.Is) + " " 
				+ quote(branch);
	}

}
