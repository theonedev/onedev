package io.onedev.server.search.entity.codecomment;

import java.util.Objects;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import io.onedev.server.model.CodeComment;
import io.onedev.server.model.User;
import io.onedev.server.search.entity.EntityCriteria;

public class CreatedByCriteria extends EntityCriteria<CodeComment> {

	private static final long serialVersionUID = 1L;

	private final User user;
	
	public CreatedByCriteria(User user) {
		this.user = user;
	}

	@Override
	public Predicate getPredicate(Root<CodeComment> root, CriteriaBuilder builder) {
		Path<User> attribute = root.get(CodeComment.PROP_USER);
		return builder.equal(attribute, user);
	}

	@Override
	public boolean matches(CodeComment comment) {
		return Objects.equals(comment.getUser(), user);
	}

	@Override
	public String toStringWithoutParens() {
		return CodeCommentQuery.getRuleName(CodeCommentQueryLexer.CreatedBy) + " " + quote(user.getName());
	}

}
