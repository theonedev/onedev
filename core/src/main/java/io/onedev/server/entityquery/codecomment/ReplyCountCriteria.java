package io.onedev.server.entityquery.codecomment;

import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import io.onedev.server.entityquery.EntityCriteria;
import io.onedev.server.entityquery.QueryBuildContext;
import io.onedev.server.model.CodeComment;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.CodeCommentConstants;

public class ReplyCountCriteria extends EntityCriteria<CodeComment> {

	private static final long serialVersionUID = 1L;

	private final int operator;
	
	private final int value;
	
	public ReplyCountCriteria(int value, int operator) {
		this.operator = operator;
		this.value = value;
	}

	@Override
	public Predicate getPredicate(Project project, QueryBuildContext<CodeComment> context) {
		Path<Integer> attribute = context.getRoot().get(CodeCommentConstants.ATTR_REPLY_COUNT);
		if (operator == CodeCommentQueryLexer.Is)
			return context.getBuilder().equal(attribute, value);
		else if (operator == CodeCommentQueryLexer.IsLessThan)
			return context.getBuilder().lessThan(attribute, value);
		else
			return context.getBuilder().greaterThan(attribute, value);
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
	public boolean needsLogin() {
		return false;
	}

	@Override
	public String toString() {
		return CodeCommentQuery.quote(CodeCommentConstants.FIELD_REPLY_COUNT) + " " + CodeCommentQuery.getRuleName(operator) + " " + CodeCommentQuery.quote(String.valueOf(value));
	}

}
