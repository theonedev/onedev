package io.onedev.server.search.entity.pullrequest;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import io.onedev.server.model.PullRequest;

import io.onedev.server.search.entity.EntityCriteria;
import io.onedev.server.util.match.WildcardUtils;
import io.onedev.server.util.query.PullRequestQueryConstants;

public class TitleCriteria extends EntityCriteria<PullRequest> {

	private static final long serialVersionUID = 1L;

	private final String value;
	
	public TitleCriteria(String value) {
		this.value = value;
	}

	@Override
	public Predicate getPredicate(Root<PullRequest> root, CriteriaBuilder builder) {
		Expression<String> attribute = root.get(PullRequestQueryConstants.ATTR_TITLE);
		return builder.like(builder.lower(attribute), "%" + value.toLowerCase().replace('*', '%') + "%");
	}

	@Override
	public boolean matches(PullRequest request) {
		String title = request.getTitle();
		return title != null && WildcardUtils.matchString("*" + value.toLowerCase() + "*", title);
	}

	@Override
	public String toString() {
		return PullRequestQuery.quote(PullRequestQueryConstants.FIELD_TITLE) + " " 
				+ PullRequestQuery.getRuleName(PullRequestQueryLexer.Contains) + " " 
				+ PullRequestQuery.quote(value);
	}

}
