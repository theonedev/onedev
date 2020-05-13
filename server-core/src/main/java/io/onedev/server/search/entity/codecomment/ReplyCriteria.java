package io.onedev.server.search.entity.codecomment;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import io.onedev.server.model.CodeComment;
import io.onedev.server.model.CodeCommentReply;
import io.onedev.server.search.entity.EntityCriteria;
import io.onedev.server.util.match.WildcardUtils;

public class ReplyCriteria extends EntityCriteria<CodeComment> {

	private static final long serialVersionUID = 1L;

	private final String value;
	
	public ReplyCriteria(String value) {
		this.value = value;
	}

	@Override
	public Predicate getPredicate(Root<CodeComment> root, CriteriaBuilder builder) {
		Join<?, ?> join = root.join(CodeComment.PROP_REPLIES, JoinType.LEFT);
		Path<String> attribute = join.get(CodeCommentReply.PROP_CONTENT);
		join.on(builder.like(builder.lower(attribute), "%" + value.toLowerCase().replace('*', '%') + "%"));
		return join.isNotNull();
	}

	@Override
	public boolean matches(CodeComment comment) {
		for (CodeCommentReply reply: comment.getReplies()) {
			String content = reply.getContent();
			if (WildcardUtils.matchString("*" + value.toLowerCase() + "*", content))
				return true;
		}
		return false;
	}

	@Override
	public String toStringWithoutParens() {
		return quote(CodeComment.NAME_REPLY) + " " 
				+ CodeCommentQuery.getRuleName(CodeCommentQueryLexer.Contains) + " " 
				+ quote(value);
	}

}
