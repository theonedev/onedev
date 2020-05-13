package io.onedev.server.search.entity.codecomment;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import io.onedev.server.model.CodeComment;
import io.onedev.server.search.entity.EntityCriteria;

public class ReplyCountCriteria extends EntityCriteria<CodeComment> {

	private static final long serialVersionUID = 1L;

	private final int operator;
	
	private final int value;
	
	public ReplyCountCriteria(int value, int operator) {
		this.operator = operator;
		this.value = value;
	}

	@Override
	public Predicate getPredicate(Root<CodeComment> root, CriteriaBuilder builder) {
		Path<Integer> attribute = root.get(CodeComment.PROP_REPLY_COUNT);
		if (operator == CodeCommentQueryLexer.Is)
			return builder.equal(attribute, value);
		else if (operator == CodeCommentQueryLexer.IsLessThan)
			return builder.lessThan(attribute, value);
		else
			return builder.greaterThan(attribute, value);
	}

	@Override
	public boolean matches(CodeComment comment) {
		if (operator == CodeCommentQueryLexer.Is)
			return comment.getReplyCount() == value;
		else if (operator == CodeCommentQueryLexer.IsLessThan)
			return comment.getReplyCount() < value;
		else
			return comment.getReplyCount() > value;
	}

	@Override
	public String toStringWithoutParens() {
		return quote(CodeComment.NAME_REPLY_COUNT) + " " 
				+ CodeCommentQuery.getRuleName(operator) + " " 
				+ quote(String.valueOf(value));
	}

}
