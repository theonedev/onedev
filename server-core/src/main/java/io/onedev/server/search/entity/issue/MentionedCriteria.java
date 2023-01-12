package io.onedev.server.search.entity.issue;

import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueMention;
import io.onedev.server.model.User;
import io.onedev.server.util.criteria.Criteria;

import javax.persistence.criteria.*;

public class MentionedCriteria extends Criteria<Issue> {

	private static final long serialVersionUID = 1L;

	private final User user;
	
	public MentionedCriteria(User user) {
		this.user = user;
	}
	
	@Override
	public Predicate getPredicate(CriteriaQuery<?> query, From<Issue, Issue> from, CriteriaBuilder builder) {
		Subquery<IssueMention> mentionQuery = query.subquery(IssueMention.class);
		Root<IssueMention> mention = mentionQuery.from(IssueMention.class);
		mentionQuery.select(mention);
		mentionQuery.where(builder.and(
				builder.equal(mention.get(IssueMention.PROP_ISSUE), from),
				builder.equal(mention.get(IssueMention.PROP_USER), user)));
		return builder.exists(mentionQuery);
	}

	@Override
	public boolean matches(Issue issue) {
		return issue.getMentions().stream().anyMatch(it->it.getUser().equals(user));
	}

	@Override
	public String toStringWithoutParens() {
		return IssueQuery.getRuleName(IssueQueryLexer.Mentioned) + " " + quote(user.getName());
	}

}
