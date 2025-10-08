package io.onedev.server.search.entity.codecomment;

import org.jspecify.annotations.Nullable;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import io.onedev.server.model.CodeComment;
import io.onedev.server.util.ProjectScope;
import io.onedev.server.util.criteria.Criteria;

public class ReplyCountCriteria extends Criteria<CodeComment> {

	private static final long serialVersionUID = 1L;

	private final int operator;
	
	private final int value;
	
	public ReplyCountCriteria(int value, int operator) {
		this.operator = operator;
		this.value = value;
	}

	@Override
	public Predicate getPredicate(@Nullable ProjectScope projectScope, CriteriaQuery<?> query, From<CodeComment, CodeComment> from, CriteriaBuilder builder) {
		Path<Integer> attribute = from.get(CodeComment.PROP_REPLY_COUNT);
		if (operator == CodeCommentQueryLexer.Is)
			return builder.equal(attribute, value);
		else if (operator == CodeCommentQueryLexer.IsNot)
			return builder.not(builder.equal(attribute, value));
		else if (operator == CodeCommentQueryLexer.IsLessThan)
			return builder.lessThan(attribute, value);
		else
			return builder.greaterThan(attribute, value);
	}

	@Override
	public boolean matches(CodeComment comment) {
		if (operator == CodeCommentQueryLexer.Is)
			return comment.getReplyCount() == value;
		else if (operator == CodeCommentQueryLexer.IsNot)
			return comment.getReplyCount() != value;
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
