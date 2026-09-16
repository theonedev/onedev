package io.onedev.server.search.entity.codecomment;

import org.jspecify.annotations.Nullable;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Predicate;

import io.onedev.commons.utils.match.WildcardUtils;
import io.onedev.server.model.CodeComment;
import io.onedev.server.util.ProjectScope;
import io.onedev.server.util.criteria.Criteria;

public class ContentCriteria extends Criteria<CodeComment> {

	private static final long serialVersionUID = 1L;

	private final String value;
	
	public ContentCriteria(String value) {
		this.value = value;
	}

	@Override
	public Predicate getPredicate(@Nullable ProjectScope projectScope, CriteriaQuery<?> query, From<CodeComment, CodeComment> from, CriteriaBuilder builder) {
		Expression<String> attribute = from.get(CodeComment.PROP_CONTENT);
		return builder.like(builder.lower(attribute), "%" + value.toLowerCase().replace('*', '%') + "%");
	}

	@Override
	public boolean matches(CodeComment comment) {
		String content = comment.getContent();
		return WildcardUtils.matchString("*" + value.toLowerCase() + "*", content);
	}

	@Override
	public String toStringWithoutParens() {
		return quote(CodeComment.NAME_CONTENT) + " " 
				+ CodeCommentQuery.getRuleName(CodeCommentQueryLexer.Contains) + " " 
				+ quote(value);
	}

}
