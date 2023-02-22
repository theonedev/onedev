package io.onedev.server.search.entity.pullrequest;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestComment;
import io.onedev.server.model.User;
import io.onedev.server.util.criteria.Criteria;

import javax.persistence.criteria.*;

public class CommentedByMeCriteria extends Criteria<PullRequest> {

	private static final long serialVersionUID = 1L;

	@Override
	public Predicate getPredicate(CriteriaQuery<?> query, From<PullRequest, PullRequest> from, CriteriaBuilder builder) {
		if (User.get() != null) {
			Subquery<PullRequestComment> commentQuery = query.subquery(PullRequestComment.class);
			Root<PullRequestComment> comment = commentQuery.from(PullRequestComment.class);
			commentQuery.select(comment);
			commentQuery.where(builder.and(
					builder.equal(comment.get(PullRequestComment.PROP_REQUEST), from),
					builder.equal(comment.get(PullRequestComment.PROP_USER), User.get())));
			return builder.exists(commentQuery);
		} else {
			throw new ExplicitException("Please login to perform this query");
		}
	}

	@Override
	public boolean matches(PullRequest request) {
		if (User.get() != null)
			return request.getComments().stream().anyMatch(it->it.getUser().equals(User.get()));
		else
			throw new ExplicitException("Please login to perform this query");
	}

	@Override
	public String toStringWithoutParens() {
		return PullRequestQuery.getRuleName(PullRequestQueryLexer.CommentedByMe);
	}

}
