package io.onedev.server.entityquery.codecomment;

import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import io.onedev.server.entityquery.EntityCriteria;
import io.onedev.server.entityquery.QueryBuildContext;
import io.onedev.server.model.CodeComment;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.codecomment.CodeCommentConstants;
import io.onedev.server.entityquery.codecomment.CodeCommentQueryLexer;

public class ContentCriteria extends EntityCriteria<CodeComment> {

	private static final long serialVersionUID = 1L;

	private final String value;
	
	public ContentCriteria(String value) {
		this.value = value;
	}

	@Override
	public Predicate getPredicate(Project project, QueryBuildContext<CodeComment> context) {
		Path<String> attribute = context.getRoot().get(CodeCommentConstants.ATTR_CONTENT);
		return context.getBuilder().like(context.getBuilder().lower(attribute), "%" + value.toLowerCase() + "%");
	}

	@Override
	public boolean matches(CodeComment comment) {
		return comment.getContent().toLowerCase().contains(value.toLowerCase());
	}

	@Override
	public boolean needsLogin() {
		return false;
	}

	@Override
	public String toString() {
		return CodeCommentQuery.quote(CodeCommentConstants.FIELD_CONTENT) + " " + CodeCommentQuery.getRuleName(CodeCommentQueryLexer.Contains) + " " + CodeCommentQuery.quote(value);
	}

}
