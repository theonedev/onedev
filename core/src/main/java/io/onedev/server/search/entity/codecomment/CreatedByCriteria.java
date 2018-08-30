package io.onedev.server.search.entity.codecomment;

import java.util.Objects;

import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import io.onedev.server.search.entity.codecomment.CodeCommentQueryLexer;
import io.onedev.server.model.CodeComment;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.model.support.codecomment.CodeCommentConstants;
import io.onedev.server.search.entity.EntityCriteria;
import io.onedev.server.search.entity.QueryBuildContext;

public class CreatedByCriteria extends EntityCriteria<CodeComment> {

	private static final long serialVersionUID = 1L;

	private final User value;
	
	public CreatedByCriteria(User value) {
		this.value = value;
	}

	@Override
	public Predicate getPredicate(Project project, QueryBuildContext<CodeComment> context, User user) {
		Path<User> attribute = context.getRoot().get(CodeCommentConstants.ATTR_USER);
		return context.getBuilder().equal(attribute, value);
	}

	@Override
	public boolean matches(CodeComment comment, User user) {
		return Objects.equals(comment.getUser(), value);
	}

	@Override
	public boolean needsLogin() {
		return false;
	}

	@Override
	public String toString() {
		return CodeCommentQuery.getRuleName(CodeCommentQueryLexer.CreatedBy) + " " + CodeCommentQuery.quote(value.getName());
	}

}
