package io.onedev.server.search.entity.codecomment;

import org.jspecify.annotations.Nullable;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Predicate;

import io.onedev.server.model.CodeComment;
import io.onedev.server.util.ProjectScope;

public class ResolvedCriteria extends StatusCriteria {

	private static final long serialVersionUID = 1L;

	@Override
	public Predicate getPredicate(@Nullable ProjectScope projectScope, CriteriaQuery<?> query, From<CodeComment, CodeComment> from, CriteriaBuilder builder) {
		return builder.equal(from.get(CodeComment.PROP_RESOLVED), true);
	}

	@Override
	public boolean matches(CodeComment comment) {
		return comment.isResolved();
	}

	@Override
	public String toStringWithoutParens() {
		return CodeCommentQuery.getRuleName(CodeCommentQueryLexer.Resolved);
	}

}
