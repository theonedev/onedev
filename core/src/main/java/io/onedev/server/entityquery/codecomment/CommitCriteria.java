package io.onedev.server.entityquery.codecomment;

import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import io.onedev.server.entityquery.EntityCriteria;
import io.onedev.server.entityquery.QueryBuildContext;
import io.onedev.server.model.CodeComment;
import io.onedev.server.model.Project;

public class CommitCriteria extends EntityCriteria<CodeComment>  {

	private static final long serialVersionUID = 1L;

	private final String value;
	
	public CommitCriteria(String value) {
		this.value = value;
	}

	@Override
	public Predicate getPredicate(Project project, QueryBuildContext<CodeComment> context) {
		Path<?> attribute = CodeCommentQuery.getPath(context.getRoot(), CodeComment.FIELD_PATHS.get(CodeComment.FIELD_COMMIT));
		return context.getBuilder().equal(attribute, value);
	}

	@Override
	public boolean matches(CodeComment comment) {
		return comment.getMarkPos().getCommit().equals(value);
	}

	@Override
	public boolean needsLogin() {
		return false;
	}

	@Override
	public String toString() {
		return CodeCommentQuery.quote(CodeComment.FIELD_COMMIT) + " " + CodeCommentQuery.getRuleName(CodeCommentQueryLexer.Is) + " " + CodeCommentQuery.quote(value);
	}

}
