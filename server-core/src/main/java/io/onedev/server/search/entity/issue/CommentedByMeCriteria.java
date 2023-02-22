package io.onedev.server.search.entity.issue;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueComment;
import io.onedev.server.model.User;
import io.onedev.server.util.criteria.Criteria;

import javax.persistence.criteria.*;

public class CommentedByMeCriteria extends Criteria<Issue> {

	private static final long serialVersionUID = 1L;

	@Override
	public Predicate getPredicate(CriteriaQuery<?> query, From<Issue, Issue> from, CriteriaBuilder builder) {
		if (User.get() != null) {
			Subquery<IssueComment> commentQuery = query.subquery(IssueComment.class);
			Root<IssueComment> comment = commentQuery.from(IssueComment.class);
			commentQuery.select(comment);
			commentQuery.where(builder.and(
					builder.equal(comment.get(IssueComment.PROP_ISSUE), from),
					builder.equal(comment.get(IssueComment.PROP_USER), User.get())));
			return builder.exists(commentQuery);
		} else {
			throw new ExplicitException("Please login to perform this query");
		}
	}

	@Override
	public boolean matches(Issue issue) {
		if (User.get() != null)
			return issue.getComments().stream().anyMatch(it->it.getUser().equals(User.get()));
		else
			throw new ExplicitException("Please login to perform this query");
	}

	@Override
	public String toStringWithoutParens() {
		return IssueQuery.getRuleName(IssueQueryLexer.CommentedByMe);
	}

}
