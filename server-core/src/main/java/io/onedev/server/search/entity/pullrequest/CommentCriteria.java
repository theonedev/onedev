package io.onedev.server.search.entity.pullrequest;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.From;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import io.onedev.server.model.CodeCommentRelation;
import io.onedev.server.model.CodeCommentReply;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestComment;

import io.onedev.server.search.entity.EntityCriteria;
import io.onedev.server.util.query.CodeCommentQueryConstants;
import io.onedev.server.util.query.PullRequestQueryConstants;

public class CommentCriteria extends EntityCriteria<PullRequest> {

	private static final long serialVersionUID = 1L;

	private final String value;
	
	public CommentCriteria(String value) {
		this.value = value;
	}

	@Override
	public Predicate getPredicate(Root<PullRequest> root, CriteriaBuilder builder) {
		From<?, ?> join = root.join(PullRequestQueryConstants.ATTR_COMMENTS, JoinType.LEFT);
		Path<String> attribute = join.get(PullRequestComment.ATTR_CONTENT);
		Predicate commentPredicate = builder.like(builder.lower(attribute), "%" + value.toLowerCase() + "%");
		
		join = root
				.join(PullRequestQueryConstants.ATTR_CODE_COMMENT_RELATIONS, JoinType.LEFT)
				.join(CodeCommentRelation.ATTR_COMMENT, JoinType.LEFT);
		attribute = join.get(CodeCommentQueryConstants.ATTR_CONTENT);
		Predicate codeCommentPredicate = builder.like(builder.lower(attribute), "%" + value.toLowerCase() + "%");
		
		join = root
				.join(PullRequestQueryConstants.ATTR_CODE_COMMENT_RELATIONS, JoinType.LEFT) 
				.join(CodeCommentRelation.ATTR_COMMENT, JoinType.LEFT) 
				.join(CodeCommentQueryConstants.ATTR_REPLIES, JoinType.LEFT);
		attribute = join.get(CodeCommentReply.ATTR_CONTENT);
		Predicate codeCommentReplyPredicate = builder.like(builder.lower(attribute), "%" + value.toLowerCase() + "%");
		
		return builder.or(commentPredicate, codeCommentPredicate, codeCommentReplyPredicate);
	}

	@Override
	public boolean matches(PullRequest request) {
		for (PullRequestComment comment: request.getComments()) { 
			if (comment.getContent().toLowerCase().contains(value.toLowerCase()))
				return true;
		}
		for (CodeCommentRelation relation: request.getCodeCommentRelations()) {
			if (relation.getComment().getContent().toLowerCase().contains(value.toLowerCase()))
				return true;
			for (CodeCommentReply reply: relation.getComment().getReplies()) {
				if (reply.getContent().toLowerCase().contains(value.toLowerCase()))
					return true;
			}
		}
		return false;
	}

	@Override
	public String toString() {
		return PullRequestQuery.quote(PullRequestQueryConstants.FIELD_COMMENT) + " " 
				+ PullRequestQuery.getRuleName(PullRequestQueryLexer.Contains) + " " 
				+ PullRequestQuery.quote(value);
	}

}
