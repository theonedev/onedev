package io.onedev.server.search.entity.codecomment;

import io.onedev.server.model.CodeComment;
import io.onedev.server.model.CodeCommentReply;
import io.onedev.server.util.criteria.Criteria;
import io.onedev.server.util.match.WildcardUtils;

import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;

public class ReplyCriteria extends Criteria<CodeComment> {

	private static final long serialVersionUID = 1L;

	private final String value;
	
	public ReplyCriteria(String value) {
		this.value = value;
	}

	@Override
	public Predicate getPredicate(CriteriaQuery<?> query, From<CodeComment, CodeComment> from, CriteriaBuilder builder) {
		Subquery<CodeCommentReply> replyQuery = query.subquery(CodeCommentReply.class);
		Root<CodeCommentReply> replyRoot = replyQuery.from(CodeCommentReply.class);
		replyQuery.select(replyRoot);

		List<Predicate> predicates = new ArrayList<>();
		predicates.add(builder.equal(replyRoot.get(CodeCommentReply.PROP_COMMENT), from));
		predicates.add(builder.like(
				builder.lower(replyRoot.get(CodeCommentReply.PROP_CONTENT)),
				"%" + value.toLowerCase() + "%"));

		return builder.exists(replyQuery.where(builder.and(predicates.toArray(new Predicate[0]))));
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
