package io.onedev.server.search.entity.codecomment;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import io.onedev.server.model.CodeComment;

import io.onedev.server.search.entity.EntityCriteria;
import io.onedev.server.util.match.WildcardUtils;
import io.onedev.server.util.query.CodeCommentQueryConstants;

public class PathCriteria extends EntityCriteria<CodeComment>  {

	private static final long serialVersionUID = 1L;

	private final String value;
	
	public PathCriteria(String value) {
		this.value = value;
	}

	@Override
	public Predicate getPredicate(Root<CodeComment> root, CriteriaBuilder builder) {
		Path<String> attribute = CodeCommentQuery.getPath(root, CodeCommentQueryConstants.ATTR_PATH);
		String normalized = value.toLowerCase().replace('*', '%');
		if (normalized.endsWith("/"))
			normalized += "%";
		return builder.like(builder.lower(attribute), normalized);
	}
	
	@Override
	public boolean matches(CodeComment comment) {
		return WildcardUtils.matchPath(value.toLowerCase(), comment.getMarkPos().getPath().toLowerCase());
	}

	@Override
	public String toString() {
		return CodeCommentQuery.quote(CodeCommentQueryConstants.FIELD_PATH) + " " 
				+ CodeCommentQuery.getRuleName(CodeCommentQueryLexer.Is) + " " 
				+ CodeCommentQuery.quote(value);
	}

}
