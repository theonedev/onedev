package io.onedev.server.search.entity.codecomment;

import io.onedev.server.model.CodeComment;
import io.onedev.server.model.CodeCommentMention;
import io.onedev.server.model.User;
import io.onedev.server.util.criteria.Criteria;

import javax.persistence.criteria.*;

public class MentionedCriteria extends Criteria<CodeComment> {

	private static final long serialVersionUID = 1L;

	private final User user;
	
	public MentionedCriteria(User user) {
		this.user = user;
	}
	
	@Override
	public Predicate getPredicate(CriteriaQuery<?> query, From<CodeComment, CodeComment> from, CriteriaBuilder builder) {
		Subquery<CodeCommentMention> mentionQuery = query.subquery(CodeCommentMention.class);
		Root<CodeCommentMention> mention = mentionQuery.from(CodeCommentMention.class);
		mentionQuery.select(mention);
		mentionQuery.where(builder.and(
				builder.equal(mention.get(CodeCommentMention.PROP_COMMENT), from),
				builder.equal(mention.get(CodeCommentMention.PROP_USER), user)));
		return builder.exists(mentionQuery);
	}

	@Override
	public boolean matches(CodeComment comment) {
		return comment.getMentions().stream().anyMatch(it->it.getUser().equals(user));
	}

	@Override
	public String toStringWithoutParens() {
		return CodeCommentQuery.getRuleName(CodeCommentQueryLexer.Mentioned) + " " + quote(user.getName());
	}

}
