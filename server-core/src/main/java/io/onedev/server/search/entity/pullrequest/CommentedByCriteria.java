package io.onedev.server.search.entity.pullrequest;

import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestComment;
import io.onedev.server.model.User;
import io.onedev.server.util.criteria.Criteria;

import javax.persistence.criteria.*;

public class CommentedByCriteria extends Criteria<PullRequest> {

	private static final long serialVersionUID = 1L;

	private final User user;
	
	public CommentedByCriteria(User user) {
		this.user = user;
	}
	
	@Override
	public Predicate getPredicate(CriteriaQuery<?> query, From<PullRequest, PullRequest> from, CriteriaBuilder builder) {
		Subquery<PullRequestComment> commentQuery = query.subquery(PullRequestComment.class);
		Root<PullRequestComment> comment = commentQuery.from(PullRequestComment.class);
		commentQuery.select(comment);
		commentQuery.where(builder.and(
				builder.equal(comment.get(PullRequestComment.PROP_REQUEST), from),
				builder.equal(comment.get(PullRequestComment.PROP_USER), user)));
		return builder.exists(commentQuery);
	}

	@Override
	public boolean matches(PullRequest request) {
		return request.getComments().stream().anyMatch(it->it.getUser().equals(user));
	}

	@Override
	public String toStringWithoutParens() {
		return PullRequestQuery.getRuleName(PullRequestQueryLexer.CommentedBy) + " " + quote(user.getName());
	}

}
