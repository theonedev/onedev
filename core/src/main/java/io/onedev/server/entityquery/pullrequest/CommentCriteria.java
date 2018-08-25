package io.onedev.server.entityquery.pullrequest;

import javax.persistence.criteria.From;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import io.onedev.server.entityquery.QueryBuildContext;
import io.onedev.server.model.CodeCommentRelation;
import io.onedev.server.model.CodeCommentReply;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestComment;
import io.onedev.server.model.support.codecomment.CodeCommentConstants;
import io.onedev.server.model.support.pullrequest.PullRequestConstants;

public class CommentCriteria extends PullRequestCriteria {

	private static final long serialVersionUID = 1L;

	private final String value;
	
	public CommentCriteria(String value) {
		this.value = value;
	}

	@Override
	public Predicate getPredicate(Project project, QueryBuildContext<PullRequest> context) {
		From<?, ?> join = context.getJoin(PullRequestConstants.ATTR_COMMENTS);
		Path<String> attribute = join.get(PullRequestComment.ATTR_CONTENT);
		Predicate commentPredicate = context.getBuilder().like(context.getBuilder().lower(attribute), "%" + value.toLowerCase() + "%");
		
		join = context.getJoin(PullRequestConstants.ATTR_CODE_COMMENT_RELATIONS + 
				"." + CodeCommentRelation.ATTR_COMMENT);
		attribute = join.get(CodeCommentConstants.ATTR_CONTENT);
		Predicate codeCommentPredicate = context.getBuilder().like(context.getBuilder().lower(attribute), "%" + value.toLowerCase() + "%");
		
		join = context.getJoin(PullRequestConstants.ATTR_CODE_COMMENT_RELATIONS + 
				"." + CodeCommentRelation.ATTR_COMMENT + 
				"." + CodeCommentConstants.ATTR_REPLIES);
		attribute = join.get(CodeCommentReply.ATTR_CONTENT);
		Predicate codeCommentReplyPredicate = context.getBuilder().like(context.getBuilder().lower(attribute), "%" + value.toLowerCase() + "%");
		
		return context.getBuilder().or(commentPredicate, codeCommentPredicate, codeCommentReplyPredicate);
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
	public boolean needsLogin() {
		return false;
	}

	@Override
	public String toString() {
		return PullRequestQuery.quote(PullRequestConstants.FIELD_COMMENT) + " " + PullRequestQuery.getRuleName(PullRequestQueryLexer.Contains) + " " + PullRequestQuery.quote(value);
	}

}
