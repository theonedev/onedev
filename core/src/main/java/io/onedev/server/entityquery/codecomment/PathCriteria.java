package io.onedev.server.entityquery.codecomment;

import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import io.onedev.server.entityquery.EntityCriteria;
import io.onedev.server.entityquery.QueryBuildContext;
import io.onedev.server.model.CodeComment;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.CodeCommentConstants;
import io.onedev.utils.stringmatch.WildcardUtils;

public class PathCriteria extends EntityCriteria<CodeComment>  {

	private static final long serialVersionUID = 1L;

	private final String value;
	
	public PathCriteria(String value) {
		this.value = value;
	}

	@Override
	public Predicate getPredicate(Project project, QueryBuildContext<CodeComment> context) {
		Path<String> attribute = CodeCommentQuery.getPath(context.getRoot(), CodeCommentConstants.ATTR_PATH);
		String normalized = value.toLowerCase().replace('*', '%');
		if (normalized.endsWith("/"))
			normalized += "%";
		return context.getBuilder().like(context.getBuilder().lower(attribute), normalized);
	}
	
	@Override
	public boolean matches(CodeComment comment) {
		String normalized = value.toLowerCase();
		if (normalized.endsWith("/"))
			normalized += "*";
		return WildcardUtils.matchString(normalized, comment.getMarkPos().getPath().toLowerCase());
	}

	@Override
	public boolean needsLogin() {
		return false;
	}

	@Override
	public String toString() {
		return CodeCommentQuery.quote(CodeCommentConstants.FIELD_PATH) + " " + CodeCommentQuery.getRuleName(CodeCommentQueryLexer.Is) + " " + CodeCommentQuery.quote(value);
	}

}
