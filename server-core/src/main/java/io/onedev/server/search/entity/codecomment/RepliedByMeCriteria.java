package io.onedev.server.search.entity.codecomment;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.model.CodeComment;
import io.onedev.server.model.CodeCommentReply;
import io.onedev.server.model.User;
import io.onedev.server.util.criteria.Criteria;

import javax.persistence.criteria.*;

public class RepliedByMeCriteria extends Criteria<CodeComment> {

	private static final long serialVersionUID = 1L;

	@Override
	public Predicate getPredicate(CriteriaQuery<?> query, From<CodeComment, CodeComment> from, CriteriaBuilder builder) {
		if (User.get() != null) {
			Subquery<CodeCommentReply> replyQuery = query.subquery(CodeCommentReply.class);
			Root<CodeCommentReply> reply = replyQuery.from(CodeCommentReply.class);
			replyQuery.select(reply);
			replyQuery.where(builder.and(
					builder.equal(reply.get(CodeCommentReply.PROP_COMMENT), from),
					builder.equal(reply.get(CodeCommentReply.PROP_USER), User.get())));
			return builder.exists(replyQuery);
		} else {
			throw new ExplicitException("Please login to perform this query");
		}
	}

	@Override
	public boolean matches(CodeComment comment) {
		if (User.get() != null)
			return comment.getReplies().stream().anyMatch(it->it.getUser().equals(User.get()));
		else
			throw new ExplicitException("Please login to perform this query");
	}

	@Override
	public String toStringWithoutParens() {
		return CodeCommentQuery.getRuleName(CodeCommentQueryLexer.RepliedByMe);
	}

}
