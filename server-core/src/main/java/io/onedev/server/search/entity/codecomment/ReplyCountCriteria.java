package io.onedev.server.search.entity.codecomment;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import io.onedev.server.model.CodeComment;
import io.onedev.server.model.User;
import io.onedev.server.search.entity.EntityCriteria;
import io.onedev.server.util.query.CodeCommentQueryConstants;

public class ReplyCountCriteria extends EntityCriteria<CodeComment> {

	private static final long serialVersionUID = 1L;

	private final int operator;
	
	private final int value;
	
	public ReplyCountCriteria(int value, int operator) {
		this.operator = operator;
		this.value = value;
	}

	@Override
	public Predicate getPredicate(Root<CodeComment> root, CriteriaBuilder builder, User user) {
		Path<Integer> attribute = root.get(CodeCommentQueryConstants.ATTR_REPLY_COUNT);
		if (operator == CodeCommentQueryLexer.Is)
			return builder.equal(attribute, value);
		else if (operator == CodeCommentQueryLexer.IsLessThan)
			return builder.lessThan(attribute, value);
		else
			return builder.greaterThan(attribute, value);
	}

	@Override
	public boolean matches(CodeComment comment, User user) {
		if (operator == CodeCommentQueryLexer.Is)
			return comment.getReplyCount() == value;
		else if (operator == CodeCommentQueryLexer.IsLessThan)
			return comment.getReplyCount() < value;
		else
			return comment.getReplyCount() > value;
	}

	@Override
	public boolean needsLogin() {
		return false;
	}

	@Override
	public String toString() {
		return CodeCommentQuery.quote(CodeCommentQueryConstants.FIELD_REPLY_COUNT) + " " + CodeCommentQuery.getRuleName(operator) + " " + CodeCommentQuery.quote(String.valueOf(value));
	}

}
