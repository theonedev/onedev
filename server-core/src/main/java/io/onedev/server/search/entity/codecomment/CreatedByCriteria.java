package io.onedev.server.search.entity.codecomment;

import java.util.Objects;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import io.onedev.server.model.CodeComment;
import io.onedev.server.model.User;
import io.onedev.server.search.entity.EntityCriteria;
import io.onedev.server.search.entity.EntityQuery;
import io.onedev.server.util.query.CodeCommentQueryConstants;

public class CreatedByCriteria extends EntityCriteria<CodeComment> {

	private static final long serialVersionUID = 1L;

	private final User user;
	
	private final String value;
	
	public CreatedByCriteria(String value) {
		user = EntityQuery.getUser(value);
		this.value = value;
	}

	@Override
	public Predicate getPredicate(Root<CodeComment> root, CriteriaBuilder builder, User user) {
		Path<User> attribute = root.get(CodeCommentQueryConstants.ATTR_USER);
		return builder.equal(attribute, this.user);
	}

	@Override
	public boolean matches(CodeComment comment, User user) {
		return Objects.equals(comment.getUser(), this.user);
	}

	@Override
	public boolean needsLogin() {
		return false;
	}

	@Override
	public String toString() {
		return CodeCommentQuery.getRuleName(CodeCommentQueryLexer.CreatedBy) 
				+ " " + CodeCommentQuery.quote(value);
	}

}
