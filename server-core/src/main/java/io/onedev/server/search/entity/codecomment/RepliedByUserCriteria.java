package io.onedev.server.search.entity.codecomment;

import org.jspecify.annotations.Nullable;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import io.onedev.server.model.CodeComment;
import io.onedev.server.model.CodeCommentReply;
import io.onedev.server.model.User;
import io.onedev.server.util.ProjectScope;
import io.onedev.server.util.criteria.Criteria;

public class RepliedByUserCriteria extends Criteria<CodeComment> {

	private static final long serialVersionUID = 1L;

	private final User user;
	
	public RepliedByUserCriteria(User user) {
		this.user = user;
	}
	
	@Override
	public Predicate getPredicate(@Nullable ProjectScope projectScope, CriteriaQuery<?> query, From<CodeComment, CodeComment> from, CriteriaBuilder builder) {
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
