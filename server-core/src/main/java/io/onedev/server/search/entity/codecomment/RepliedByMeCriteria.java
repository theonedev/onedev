package io.onedev.server.search.entity.codecomment;

import static io.onedev.server.web.translation.Translation._T;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;

import org.jspecify.annotations.Nullable;

import io.onedev.server.exception.NotAcceptableException;
import io.onedev.server.model.CodeComment;
import io.onedev.server.model.CodeCommentReply;
import io.onedev.server.model.User;
import io.onedev.server.util.ProjectScope;
import io.onedev.server.util.criteria.Criteria;

public class RepliedByMeCriteria extends Criteria<CodeComment> {

	private static final long serialVersionUID = 1L;

	@Override
	public Predicate getPredicate(@Nullable ProjectScope projectScope, CriteriaQuery<?> query, From<CodeComment, CodeComment> from, CriteriaBuilder builder) {
		if (User.get() != null) {
			Subquery<CodeCommentReply> replyQuery = query.subquery(CodeCommentReply.class);
			Root<CodeCommentReply> reply = replyQuery.from(CodeCommentReply.class);
			replyQuery.select(reply);
			replyQuery.where(builder.and(
					builder.equal(reply.get(CodeCommentReply.PROP_COMMENT), from),
					builder.equal(reply.get(CodeCommentReply.PROP_USER), User.get())));
			return builder.exists(replyQuery);
		} else {
			throw new NotAcceptableException(_T("Please login to perform this query"));
		}
	}

	@Override
	public boolean matches(CodeComment comment) {
		if (User.get() != null)
			return comment.getReplies().stream().anyMatch(it->it.getUser().equals(User.get()));
		else
			throw new NotAcceptableException(_T("Please login to perform this query"));
	}

	@Override
	public String toStringWithoutParens() {
		return CodeCommentQuery.getRuleName(CodeCommentQueryLexer.RepliedByMe);
	}

}
