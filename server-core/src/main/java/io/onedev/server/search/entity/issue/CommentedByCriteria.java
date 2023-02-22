package io.onedev.server.search.entity.issue;

import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueComment;
import io.onedev.server.model.User;
import io.onedev.server.util.criteria.Criteria;

import javax.persistence.criteria.*;

public class CommentedByCriteria extends Criteria<Issue> {

	private static final long serialVersionUID = 1L;

	private final User user;
	
	public CommentedByCriteria(User user) {
		this.user = user;
	}
	
	@Override
	public Predicate getPredicate(CriteriaQuery<?> query, From<Issue, Issue> from, CriteriaBuilder builder) {
		Subquery<IssueComment> commentQuery = query.subquery(IssueComment.class);
		Root<IssueComment> comment = commentQuery.from(IssueComment.class);
		commentQuery.select(comment);
		commentQuery.where(builder.and(
				builder.equal(comment.get(IssueComment.PROP_ISSUE), from),
				builder.equal(comment.get(IssueComment.PROP_USER), user)));
		return builder.exists(commentQuery);
	}

	@Override
	public boolean matches(Issue issue) {
		return issue.getComments().stream().anyMatch(it->it.getUser().equals(user));
	}

	@Override
	public String toStringWithoutParens() {
		return IssueQuery.getRuleName(IssueQueryLexer.CommentedBy) + " " + quote(user.getName());
	}

}
