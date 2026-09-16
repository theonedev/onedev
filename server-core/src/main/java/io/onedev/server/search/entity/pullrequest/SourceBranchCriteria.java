package io.onedev.server.search.entity.pullrequest;

import org.jspecify.annotations.Nullable;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;

import io.onedev.commons.utils.match.WildcardUtils;
import io.onedev.server.model.PullRequest;
import io.onedev.server.util.ProjectScope;
import io.onedev.server.util.criteria.Criteria;

public class SourceBranchCriteria extends Criteria<PullRequest> {

	private static final long serialVersionUID = 1L;

	private final String branch;
	
	private final int operator;
	
	public SourceBranchCriteria(String value, int operator) {
		this.branch = value;
		this.operator = operator;
	}

	@Override
	public Predicate getPredicate(@Nullable ProjectScope projectScope, CriteriaQuery<?> query, From<PullRequest, PullRequest> from, CriteriaBuilder builder) {
		Path<String> attribute = from.get(PullRequest.PROP_SOURCE_BRANCH);
		String normalized = branch.toLowerCase().replace("*", "%");
		var predicate = builder.like(builder.lower(attribute), normalized);
		if (operator == PullRequestQueryLexer.IsNot)
			predicate = builder.not(predicate);
		return predicate;
	}

	@Override
	public boolean matches(PullRequest request) {
		var matches = WildcardUtils.matchString(branch.toLowerCase(), request.getSourceBranch().toLowerCase());
		if (operator == PullRequestQueryLexer.IsNot)
			matches = !matches;
		return matches;
	}

	@Override
	public String toStringWithoutParens() {
		return quote(PullRequest.NAME_SOURCE_BRANCH) + " " 
				+ PullRequestQuery.getRuleName(operator) + " " 
				+ quote(branch);
	}

}
