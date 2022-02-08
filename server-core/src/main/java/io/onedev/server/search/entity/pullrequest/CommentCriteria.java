package io.onedev.server.search.entity.pullrequest;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestChange;
import io.onedev.server.model.PullRequestComment;
import io.onedev.server.util.criteria.Criteria;

public class CommentCriteria extends Criteria<PullRequest> {

	private static final long serialVersionUID = 1L;

	private final String value;
	
	public CommentCriteria(String value) {
		this.value = value;
	}

	@Override
	public Predicate getPredicate(CriteriaQuery<?> query, From<PullRequest, PullRequest> from, CriteriaBuilder builder) {
		Join<?, ?> commentJoin = from.join(PullRequest.PROP_COMMENTS, JoinType.LEFT);
		Path<String> contentAttribute = commentJoin.get(PullRequestComment.PROP_CONTENT);
		commentJoin.on(builder.like(builder.lower(contentAttribute), "%" + value.toLowerCase() + "%"));
		
		Join<?, ?> changeJoin = from.join(PullRequest.PROP_CHANGES, JoinType.LEFT);
		Path<String> commentAttribute = changeJoin.get(PullRequestChange.PROP_COMMENT);
		changeJoin.on(builder.like(builder.lower(commentAttribute), "%" + value.toLowerCase() + "%"));
		
		return builder.or(commentJoin.isNotNull(), changeJoin.isNotNull());
	}

	@Override
	public boolean matches(PullRequest request) {
		for (PullRequestComment comment: request.getComments()) { 
			if (comment.getContent().toLowerCase().contains(value.toLowerCase()))
				return true;
		}
		for (PullRequestChange change: request.getChanges()) { 
			if (change.getComment() != null && change.getComment().toLowerCase().contains(value.toLowerCase()))
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
