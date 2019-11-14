package io.onedev.server.search.entity.codecomment;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.From;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import io.onedev.server.model.CodeComment;
import io.onedev.server.model.CodeCommentReply;
import io.onedev.server.model.User;
import io.onedev.server.search.entity.EntityCriteria;
import io.onedev.server.util.CodeCommentConstants;

public class ReplyCriteria extends EntityCriteria<CodeComment> {

	private static final long serialVersionUID = 1L;

	private final String value;
	
	public ReplyCriteria(String value) {
		this.value = value;
	}

	@Override
	public Predicate getPredicate(Root<CodeComment> root, CriteriaBuilder builder, User user) {
		From<?, ?> join = root.join(CodeCommentConstants.ATTR_REPLIES, JoinType.LEFT);
		Path<String> attribute = join.get(CodeCommentReply.ATTR_CONTENT);
		return builder.like(builder.lower(attribute), "%" + value.toLowerCase() + "%");
	}

	@Override
	public boolean matches(CodeComment comment, User user) {
		for (CodeCommentReply reply: comment.getReplies()) {
			if (reply.getContent().toLowerCase().contains(value.toLowerCase()))
				return true;
		}
		return false;
	}

	@Override
	public boolean needsLogin() {
		return false;
	}

	@Override
	public String toString() {
		return CodeCommentQuery.quote(CodeCommentConstants.FIELD_REPLY) + " " + CodeCommentQuery.getRuleName(CodeCommentQueryLexer.Contains) + " " + CodeCommentQuery.quote(value);
	}

}
