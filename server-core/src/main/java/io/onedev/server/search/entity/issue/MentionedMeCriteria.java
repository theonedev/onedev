package io.onedev.server.search.entity.issue;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueMention;
import io.onedev.server.model.User;
import io.onedev.server.util.criteria.Criteria;

import javax.persistence.criteria.*;

public class MentionedMeCriteria extends Criteria<Issue> {

	private static final long serialVersionUID = 1L;

	@Override
	public Predicate getPredicate(CriteriaQuery<?> query, From<Issue, Issue> from, CriteriaBuilder builder) {
		if (User.get() != null) {
			Subquery<IssueMention> mentionQuery = query.subquery(IssueMention.class);
			Root<IssueMention> mention = mentionQuery.from(IssueMention.class);
			mentionQuery.select(mention);
			mentionQuery.where(builder.and(
					builder.equal(mention.get(IssueMention.PROP_ISSUE), from),
					builder.equal(mention.get(IssueMention.PROP_USER), User.get())));
			return builder.exists(mentionQuery);
		} else {
			throw new ExplicitException("Please login to perform this query");
		}
	}

	@Override
	public boolean matches(Issue issue) {
		if (User.get() != null)
			return issue.getMentions().stream().anyMatch(it->it.getUser().equals(User.get()));
		else
			throw new ExplicitException("Please login to perform this query");
	}

	@Override
	public String toStringWithoutParens() {
		return IssueQuery.getRuleName(IssueQueryLexer.MentionedMe);
	}

}
