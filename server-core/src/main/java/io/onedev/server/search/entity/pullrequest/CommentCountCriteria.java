package io.onedev.server.search.entity.pullrequest;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import io.onedev.server.model.PullRequest;
import io.onedev.server.search.entity.EntityCriteria;
import io.onedev.server.util.query.PullRequestQueryConstants;

public class CommentCountCriteria extends EntityCriteria<PullRequest> {

	private static final long serialVersionUID = 1L;

	private final int operator;
	
	private final long value;
	
	public CommentCountCriteria(int value, int operator) {
		this.operator = operator;
		this.value = value;
	}

	@Override
	public Predicate getPredicate(Root<PullRequest> root, CriteriaBuilder builder) {
		Path<Long> attribute = root.get(PullRequestQueryConstants.ATTR_COMMENT_COUNT);
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
	public String asString() {
		return quote(PullRequestQueryConstants.FIELD_COMMENT_COUNT) + " " 
				+ PullRequestQuery.getRuleName(operator) + " " 
				+ quote(String.valueOf(value));
	}

}
