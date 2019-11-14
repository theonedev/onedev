package io.onedev.server.search.entity.codecomment;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import io.onedev.server.model.CodeComment;
import io.onedev.server.model.User;
import io.onedev.server.search.entity.EntityCriteria;
import io.onedev.server.util.CodeCommentConstants;

public class ContentCriteria extends EntityCriteria<CodeComment> {

	private static final long serialVersionUID = 1L;

	private final String value;
	
	public ContentCriteria(String value) {
		this.value = value;
	}

	@Override
	public Predicate getPredicate(Root<CodeComment> root, CriteriaBuilder builder, User user) {
		Path<String> attribute = root.get(CodeCommentConstants.ATTR_CONTENT);
		return builder.like(builder.lower(attribute), "%" + value.toLowerCase() + "%");
	}

	@Override
	public boolean matches(CodeComment comment, User user) {
		return comment.getContent().toLowerCase().contains(value.toLowerCase());
	}

	@Override
	public boolean needsLogin() {
		return false;
	}

	@Override
	public String toString() {
		return CodeCommentQuery.quote(CodeCommentConstants.FIELD_CONTENT) + " " + CodeCommentQuery.getRuleName(CodeCommentQueryLexer.Contains) + " " + CodeCommentQuery.quote(value);
	}

}
