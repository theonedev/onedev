package io.onedev.server.search.entity.codecomment;

import java.util.Objects;

import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import io.onedev.server.model.CodeComment;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.model.support.codecomment.CodeCommentConstants;
import io.onedev.server.search.entity.EntityCriteria;
import io.onedev.server.search.entity.QueryBuildContext;

public class CreatedByMeCriteria extends EntityCriteria<CodeComment> {

	private static final long serialVersionUID = 1L;

	@Override
	public Predicate getPredicate(Project project, QueryBuildContext<CodeComment> context, User user) {
		Path<?> attribute = context.getRoot().get(CodeCommentConstants.ATTR_USER);
		return context.getBuilder().equal(attribute, user);
	}

	@Override
	public boolean matches(CodeComment comment, User user) {
		return Objects.equals(comment.getUser(), user);
	}

	@Override
	public boolean needsLogin() {
		return true;
	}

	@Override
	public String toString() {
		return CodeCommentQuery.getRuleName(CodeCommentQueryLexer.CreatedByMe);
	}

}
