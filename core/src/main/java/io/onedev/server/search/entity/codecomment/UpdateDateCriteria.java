package io.onedev.server.search.entity.codecomment;

import java.util.Date;

import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import io.onedev.server.search.entity.codecomment.CodeCommentQueryLexer;
import io.onedev.server.model.CodeComment;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.model.support.codecomment.CodeCommentConstants;
import io.onedev.server.search.entity.EntityCriteria;
import io.onedev.server.search.entity.QueryBuildContext;

public class UpdateDateCriteria extends EntityCriteria<CodeComment>  {

	private static final long serialVersionUID = 1L;

	private final int operator;
	
	private final Date value;
	
	private final String rawValue;
	
	public UpdateDateCriteria(Date value, String rawValue, int operator) {
		this.operator = operator;
		this.value = value;
		this.rawValue = rawValue;
	}

	@Override
	public Predicate getPredicate(Project project, QueryBuildContext<CodeComment> context, User user) {
		Path<Date> attribute = CodeCommentQuery.getPath(context.getRoot(), CodeCommentConstants.ATTR_UPDATE_DATE);
		if (operator == CodeCommentQueryLexer.IsBefore)
			return context.getBuilder().lessThan(attribute, value);
		else
			return context.getBuilder().greaterThan(attribute, value);
	}

	@Override
	public boolean matches(CodeComment comment, User user) {
		if (operator == CodeCommentQueryLexer.IsBefore)
			return comment.getLastActivity().getDate().before(value);
		else
			return comment.getLastActivity().getDate().after(value);
	}

	@Override
	public boolean needsLogin() {
		return false;
	}

	@Override
	public String toString() {
		return CodeCommentQuery.quote(CodeCommentConstants.FIELD_UPDATE_DATE) + " " + CodeCommentQuery.getRuleName(operator) + " " + CodeCommentQuery.quote(rawValue);
	}

}
