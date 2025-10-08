package io.onedev.server.search.entity.codecomment;

import static io.onedev.commons.utils.match.WildcardUtils.matchString;

import java.util.ArrayList;
import java.util.List;

import org.jspecify.annotations.Nullable;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import io.onedev.server.model.CodeComment;
import io.onedev.server.model.CodeCommentReply;
import io.onedev.server.util.ProjectScope;
import io.onedev.server.util.criteria.Criteria;

public class ReplyCriteria extends Criteria<CodeComment> {

	private static final long serialVersionUID = 1L;

	private final String value;
	
	public ReplyCriteria(String value) {
		this.value = value;
	}

	@Override
	public Predicate getPredicate(@Nullable ProjectScope projectScope, CriteriaQuery<?> query, From<CodeComment, CodeComment> from, CriteriaBuilder builder) {
		Subquery<CodeCommentReply> replyQuery = query.subquery(CodeCommentReply.class);
		Root<CodeCommentReply> replyRoot = replyQuery.from(CodeCommentReply.class);
		replyQuery.select(replyRoot);

		List<Predicate> predicates = new ArrayList<>();
		predicates.add(builder.equal(replyRoot.get(CodeCommentReply.PROP_COMMENT), from));
		predicates.add(builder.like(
				builder.lower(replyRoot.get(CodeCommentReply.PROP_CONTENT)),
				"%" + value.toLowerCase().replace('*', '%')+ "%"));

		return builder.exists(replyQuery.where(builder.and(predicates.toArray(new Predicate[0]))));
	}

	@Override
	public boolean matches(CodeComment comment) {
		for (CodeCommentReply reply: comment.getReplies()) {
			String content = reply.getContent();
			if (matchString("*" + value.toLowerCase() + "*", content.toLowerCase()))
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
