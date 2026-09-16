package io.onedev.server.search.entity.pullrequest;

import org.jspecify.annotations.Nullable;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;

import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestMention;
import io.onedev.server.model.User;
import io.onedev.server.util.ProjectScope;
import io.onedev.server.util.criteria.Criteria;

public class MentionedUserCriteria extends Criteria<PullRequest> {

	private static final long serialVersionUID = 1L;

	private final User user;
	
	public MentionedUserCriteria(User user) {
		this.user = user;
	}
	
	@Override
	public Predicate getPredicate(@Nullable ProjectScope projectScope, CriteriaQuery<?> query, From<PullRequest, PullRequest> from, CriteriaBuilder builder) {
		Subquery<PullRequestMention> mentionQuery = query.subquery(PullRequestMention.class);
		Root<PullRequestMention> mention = mentionQuery.from(PullRequestMention.class);
		mentionQuery.select(mention);
		mentionQuery.where(builder.and(
				builder.equal(mention.get(PullRequestMention.PROP_REQUEST), from),
				builder.equal(mention.get(PullRequestMention.PROP_USER), user)));
		return builder.exists(mentionQuery);
	}

	@Override
	public boolean matches(PullRequest request) {
		return request.getMentions().stream().anyMatch(it->it.getUser().equals(user));
	}

	@Override
	public String toStringWithoutParens() {
		return PullRequestQuery.getRuleName(PullRequestQueryLexer.Mentioned) + " " + quote(user.getName());
	}

}
