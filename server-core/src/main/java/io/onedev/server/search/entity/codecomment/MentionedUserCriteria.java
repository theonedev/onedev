package io.onedev.server.search.entity.codecomment;

import org.jspecify.annotations.Nullable;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;

import io.onedev.server.model.CodeComment;
import io.onedev.server.model.CodeCommentMention;
import io.onedev.server.model.User;
import io.onedev.server.util.ProjectScope;
import io.onedev.server.util.criteria.Criteria;

public class MentionedUserCriteria extends Criteria<CodeComment> {

	private static final long serialVersionUID = 1L;

	private final User user;
	
	public MentionedUserCriteria(User user) {
		this.user = user;
	}
	
	@Override
	public Predicate getPredicate(@Nullable ProjectScope projectScope, CriteriaQuery<?> query, From<CodeComment, CodeComment> from, CriteriaBuilder builder) {
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
