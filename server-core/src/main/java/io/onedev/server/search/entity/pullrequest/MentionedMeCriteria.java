package io.onedev.server.search.entity.pullrequest;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestMention;
import io.onedev.server.model.User;
import io.onedev.server.util.criteria.Criteria;

import javax.persistence.criteria.*;

public class MentionedMeCriteria extends Criteria<PullRequest> {

	private static final long serialVersionUID = 1L;

	@Override
	public Predicate getPredicate(CriteriaQuery<?> query, From<PullRequest, PullRequest> from, CriteriaBuilder builder) {
		if (User.get() != null) {
			Subquery<PullRequestMention> mentionQuery = query.subquery(PullRequestMention.class);
			Root<PullRequestMention> mention = mentionQuery.from(PullRequestMention.class);
			mentionQuery.select(mention);
			mentionQuery.where(builder.and(
					builder.equal(mention.get(PullRequestMention.PROP_REQUEST), from),
					builder.equal(mention.get(PullRequestMention.PROP_USER), User.get())));
			return builder.exists(mentionQuery);
		} else {
			throw new ExplicitException("Please login to perform this query");
		}
	}

	@Override
	public boolean matches(PullRequest request) {
		if (User.get() != null)
			return request.getMentions().stream().anyMatch(it->it.getUser().equals(User.get()));
		else
			throw new ExplicitException("Please login to perform this query");
	}

	@Override
	public String toStringWithoutParens() {
		return PullRequestQuery.getRuleName(PullRequestQueryLexer.MentionedMe);
	}

}
