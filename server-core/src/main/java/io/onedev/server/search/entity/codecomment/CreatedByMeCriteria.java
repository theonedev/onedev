package io.onedev.server.search.entity.codecomment;

import java.util.Objects;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import io.onedev.server.model.CodeComment;
import io.onedev.server.model.User;
import io.onedev.server.search.entity.EntityCriteria;
import io.onedev.server.util.CodeCommentConstants;

public class CreatedByMeCriteria extends EntityCriteria<CodeComment> {

	private static final long serialVersionUID = 1L;

	@Override
	public Predicate getPredicate(Root<CodeComment> root, CriteriaBuilder builder, User user) {
		Path<?> attribute = root.get(CodeCommentConstants.ATTR_USER);
		return builder.equal(attribute, user);
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
