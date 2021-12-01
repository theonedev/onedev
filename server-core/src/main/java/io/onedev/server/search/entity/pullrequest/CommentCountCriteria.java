package io.onedev.server.search.entity.pullrequest;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import io.onedev.server.model.PullRequest;
import io.onedev.server.search.entity.EntityCriteria;

public class CommentCountCriteria extends EntityCriteria<PullRequest> {

	private static final long serialVersionUID = 1L;

	private final int operator;
	
	private final long value;
	
	public CommentCountCriteria(int value, int operator) {
		this.operator = operator;
		this.value = value;
	}

	@Override
	public Predicate getPredicate(CriteriaQuery<?> query, From<PullRequest, PullRequest> from, CriteriaBuilder builder) {
		Path<Long> attribute = from.get(PullRequest.PROP_COMMENT_COUNT);
		if (operator == PullRequestQueryLexer.Is)
			return builder.equal(attribute, value);
		else if (operator == PullRequestQueryLexer.IsGreaterThan)
			return builder.greaterThan(attribute, value);
		else
			return builder.lessThan(attribute, value);
	}

	@Override
	public boolean matches(PullRequest request) {
		if (operator == PullRequestQueryLexer.Is)
			return request.getCommentCount() == value;
		else if (operator == PullRequestQueryLexer.IsGreaterThan)
			return request.getCommentCount() > value;
		else
			return request.getCommentCount() < value;
	}

	@Override
	public String toStringWithoutParens() {
		return quote(PullRequest.NAME_COMMENT_COUNT) + " " 
				+ PullRequestQuery.getRuleName(operator) + " " 
				+ quote(String.valueOf(value));
	}

}
