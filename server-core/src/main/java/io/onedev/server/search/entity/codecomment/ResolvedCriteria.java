package io.onedev.server.search.entity.codecomment;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Predicate;

import io.onedev.server.model.CodeComment;
import io.onedev.server.util.criteria.Criteria;

public class ResolvedCriteria extends Criteria<CodeComment> {

	private static final long serialVersionUID = 1L;

	@Override
	public Predicate getPredicate(CriteriaQuery<?> query, From<CodeComment, CodeComment> from, CriteriaBuilder builder) {
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
