package io.onedev.server.entityquery.codecomment;

import java.util.Date;

import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import io.onedev.server.entityquery.EntityCriteria;
import io.onedev.server.entityquery.QueryBuildContext;
import io.onedev.server.model.CodeComment;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.codecomment.CodeCommentConstants;

public class CreateDateCriteria extends EntityCriteria<CodeComment> {

	private static final long serialVersionUID = 1L;

	private final int operator;
	
	private final Date value;
	
	private final String rawValue;
	
	public CreateDateCriteria(Date value, String rawValue, int operator) {
		this.operator = operator;
		this.value = value;
		this.rawValue = rawValue;
	}

	@Override
	public Predicate getPredicate(Project project, QueryBuildContext<CodeComment> context) {
		Path<Date> attribute = context.getRoot().get(CodeCommentConstants.ATTR_CREATE_DATE);
		if (operator == CodeCommentQueryLexer.IsBefore)
			return context.getBuilder().lessThan(attribute, value);
		else
			return context.getBuilder().greaterThan(attribute, value);
	}

	@Override
	public boolean matches(CodeComment comment) {
		if (operator == CodeCommentQueryLexer.IsBefore)
			return comment.getDate().before(value);
		else
			return comment.getDate().after(value);
	}

	@Override
	public boolean needsLogin() {
		return false;
	}

	@Override
	public String toString() {
		return CodeCommentQuery.quote(CodeCommentConstants.FIELD_CREATE_DATE) + " " + CodeCommentQuery.getRuleName(operator) + " " + CodeCommentQuery.quote(rawValue);
	}

}
