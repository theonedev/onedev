package io.onedev.server.search.entity.codecomment;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.model.CodeComment;
import io.onedev.server.model.CodeCommentMention;
import io.onedev.server.model.User;
import io.onedev.server.util.criteria.Criteria;

import javax.persistence.criteria.*;

public class MentionedMeCriteria extends Criteria<CodeComment> {

	private static final long serialVersionUID = 1L;

	@Override
	public Predicate getPredicate(CriteriaQuery<?> query, From<CodeComment, CodeComment> from, CriteriaBuilder builder) {
		if (User.get() != null) {
			Subquery<CodeCommentMention> mentionQuery = query.subquery(CodeCommentMention.class);
			Root<CodeCommentMention> mention = mentionQuery.from(CodeCommentMention.class);
			mentionQuery.select(mention);
			mentionQuery.where(builder.and(
					builder.equal(mention.get(CodeCommentMention.PROP_COMMENT), from),
					builder.equal(mention.get(CodeCommentMention.PROP_USER), User.get())));
			return builder.exists(mentionQuery);
		} else {
			throw new ExplicitException("Please login to perform this query");
		}
	}

	@Override
	public boolean matches(CodeComment comment) {
		if (User.get() != null)
			return comment.getMentions().stream().anyMatch(it->it.getUser().equals(User.get()));
		else
			throw new ExplicitException("Please login to perform this query");
	}

	@Override
	public String toStringWithoutParens() {
		return CodeCommentQuery.getRuleName(CodeCommentQueryLexer.MentionedMe);
	}

}
