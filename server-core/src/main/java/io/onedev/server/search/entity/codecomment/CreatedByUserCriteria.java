package io.onedev.server.search.entity.codecomment;

import java.util.Objects;

import org.jspecify.annotations.Nullable;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import io.onedev.server.model.CodeComment;
import io.onedev.server.model.User;
import io.onedev.server.util.ProjectScope;

public class CreatedByUserCriteria extends CreatedByCriteria {

	private static final long serialVersionUID = 1L;

	private final User user;
	
	public CreatedByUserCriteria(User user) {
		this.user = user;
	}

	@Override
	public User getUser() {
		return user;
	}

	@Override
	public Predicate getPredicate(@Nullable ProjectScope projectScope, CriteriaQuery<?> query, From<CodeComment, CodeComment> from, CriteriaBuilder builder) {
		Path<User> attribute = from.get(CodeComment.PROP_USER);
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
