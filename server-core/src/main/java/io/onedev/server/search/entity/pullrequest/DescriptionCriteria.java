package io.onedev.server.search.entity.pullrequest;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import io.onedev.commons.utils.match.WildcardUtils;
import io.onedev.server.model.PullRequest;

import io.onedev.server.search.entity.EntityCriteria;
import io.onedev.server.util.query.PullRequestQueryConstants;

public class DescriptionCriteria extends EntityCriteria<PullRequest> {

	private static final long serialVersionUID = 1L;

	private final String value;
	
	public DescriptionCriteria(String value) {
		this.value = value;
	}

	@Override
	public Predicate getPredicate(Root<PullRequest> root, CriteriaBuilder builder) {
		Expression<String> attribute = root.get(PullRequestQueryConstants.ATTR_DESCRIPTION);
		return builder.like(builder.lower(attribute), "%" + value.toLowerCase().replace('*', '%') + "%");
	}

	@Override
	public boolean matches(PullRequest request) {
		String description = request.getDescription();
		return description != null && WildcardUtils.matchString("*" + value.toLowerCase() + "*", description);
	}

	@Override
	public String toString() {
		return PullRequestQuery.quote(PullRequestQueryConstants.FIELD_DESCRIPTION) + " " 
				+ PullRequestQuery.getRuleName(PullRequestQueryLexer.Contains) + " " + PullRequestQuery.quote(value);
	}

}
