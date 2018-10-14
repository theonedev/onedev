package io.onedev.server.search.entity.codecomment;

import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import io.onedev.server.search.entity.codecomment.CodeCommentQueryLexer;
import io.onedev.server.util.CodeCommentConstants;
import io.onedev.server.model.CodeComment;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.search.entity.EntityCriteria;
import io.onedev.server.search.entity.QueryBuildContext;
import io.onedev.utils.stringmatch.WildcardUtils;

public class PathCriteria extends EntityCriteria<CodeComment>  {

	private static final long serialVersionUID = 1L;

	private final String value;
	
	public PathCriteria(String value) {
		this.value = value;
	}

	@Override
	public Predicate getPredicate(Project project, QueryBuildContext<CodeComment> context, User user) {
		Path<String> attribute = CodeCommentQuery.getPath(context.getRoot(), CodeCommentConstants.ATTR_PATH);
		String normalized = value.toLowerCase().replace('*', '%');
		if (normalized.endsWith("/"))
			normalized += "%";
		return context.getBuilder().like(context.getBuilder().lower(attribute), normalized);
	}
	
	@Override
	public boolean matches(CodeComment comment, User user) {
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
