package io.onedev.server.search.entity.codecomment;

import io.onedev.server.model.CodeComment;
import io.onedev.server.model.CodeCommentReply;
import io.onedev.server.model.User;
import io.onedev.server.util.criteria.Criteria;

import javax.persistence.criteria.*;

public class RepliedByCriteria extends Criteria<CodeComment> {

	private static final long serialVersionUID = 1L;

	private final User user;
	
	public RepliedByCriteria(User user) {
		this.user = user;
	}
	
	@Override
	public Predicate getPredicate(CriteriaQuery<?> query, From<CodeComment, CodeComment> from, CriteriaBuilder builder) {
		Subquery<CodeCommentReply> replyQuery = query.subquery(CodeCommentReply.class);
		Root<CodeCommentReply> reply = replyQuery.from(CodeCommentReply.class);
		replyQuery.select(reply);
		replyQuery.where(builder.and(
				builder.equal(reply.get(CodeCommentReply.PROP_COMMENT), from),
				builder.equal(reply.get(CodeCommentReply.PROP_USER), user)));
		return builder.exists(replyQuery);
	}

	@Override
	public boolean matches(CodeComment comment) {
		return comment.getReplies().stream().anyMatch(it->it.getUser().equals(user));
	}

	@Override
	public String toStringWithoutParens() {
		return CodeCommentQuery.getRuleName(CodeCommentQueryLexer.RepliedBy) + " " + quote(user.getName());
	}

}
