package io.onedev.server.search.entity.issue;

import static io.onedev.server.web.translation.Translation._T;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;

import org.jspecify.annotations.Nullable;

import io.onedev.server.exception.NotAcceptableException;
import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueComment;
import io.onedev.server.model.User;
import io.onedev.server.util.ProjectScope;
import io.onedev.server.util.criteria.Criteria;

public class CommentedByMeCriteria extends Criteria<Issue> {

	private static final long serialVersionUID = 1L;

	@Override
	public Predicate getPredicate(@Nullable ProjectScope projectScope, CriteriaQuery<?> query, From<Issue, Issue> from, CriteriaBuilder builder) {
		if (User.get() != null) {
			Subquery<IssueComment> commentQuery = query.subquery(IssueComment.class);
			Root<IssueComment> comment = commentQuery.from(IssueComment.class);
			commentQuery.select(comment);
			commentQuery.where(builder.and(
					builder.equal(comment.get(IssueComment.PROP_ISSUE), from),
					builder.equal(comment.get(IssueComment.PROP_USER), User.get())));
			return builder.exists(commentQuery);
		} else {
			throw new NotAcceptableException(_T("Please login to perform this query"));
		}
	}

	@Override
	public boolean matches(Issue issue) {
		if (User.get() != null)
			return issue.getComments().stream().anyMatch(it->it.getUser().equals(User.get()));
		else
			throw new NotAcceptableException(_T("Please login to perform this query"));
	}

	@Override
	public String toStringWithoutParens() {
		return IssueQuery.getRuleName(IssueQueryLexer.CommentedByMe);
	}

}
