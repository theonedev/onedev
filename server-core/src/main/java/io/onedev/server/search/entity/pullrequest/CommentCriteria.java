package io.onedev.server.search.entity.pullrequest;

import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestComment;
import io.onedev.server.util.criteria.Criteria;

import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;

public class CommentCriteria extends Criteria<PullRequest> {

	private static final long serialVersionUID = 1L;

	private final String value;
	
	public CommentCriteria(String value) {
		this.value = value;
	}

	@Override
	public Predicate getPredicate(CriteriaQuery<?> query, From<PullRequest, PullRequest> from, CriteriaBuilder builder) {
		Subquery<PullRequestComment> commentQuery = query.subquery(PullRequestComment.class);
		Root<PullRequestComment> commentRoot = commentQuery.from(PullRequestComment.class);
		commentQuery.select(commentRoot);

		List<Predicate> predicates = new ArrayList<>();
		predicates.add(builder.equal(commentRoot.get(PullRequestComment.PROP_REQUEST), from));
		predicates.add(builder.like(
				builder.lower(commentRoot.get(PullRequestComment.PROP_CONTENT)),
				"%" + value.toLowerCase() + "%"));

		return builder.exists(commentQuery.where(builder.and(predicates.toArray(new Predicate[0]))));
	}

	@Override
	public boolean matches(PullRequest request) {
		for (PullRequestComment comment: request.getComments()) { 
			if (comment.getContent().toLowerCase().contains(value.toLowerCase()))
				return true;
		}
		return false;
	}

	@Override
	public String toStringWithoutParens() {
		return quote(PullRequest.NAME_COMMENT) + " " 
				+ PullRequestQuery.getRuleName(PullRequestQueryLexer.Contains) + " " 
				+ quote(value);
	}

}
