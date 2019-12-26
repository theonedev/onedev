package io.onedev.server.search.entity.codecomment;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import io.onedev.server.OneException;
import io.onedev.server.model.CodeComment;
import io.onedev.server.model.User;
import io.onedev.server.search.entity.EntityCriteria;
import io.onedev.server.util.query.CodeCommentQueryConstants;

public class CreatedByMeCriteria extends EntityCriteria<CodeComment> {

	private static final long serialVersionUID = 1L;

	@Override
	public Predicate getPredicate(Root<CodeComment> root, CriteriaBuilder builder) {
		if (User.get() != null) {
			Path<?> attribute = root.get(CodeCommentQueryConstants.ATTR_USER);
			return builder.equal(attribute, User.get());
		} else {
			throw new OneException("Please login to perform this query");
		}
	}

	@Override
	public boolean matches(CodeComment comment) {
		if (User.get() != null)
			return User.get().equals(comment.getUser());
		else
			throw new OneException("Please login to perform this query");
	}

	@Override
	public String asString() {
		return CodeCommentQuery.getRuleName(CodeCommentQueryLexer.CreatedByMe);
	}

}
