package io.onedev.server.search.entity.codecomment;

import org.jspecify.annotations.Nullable;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import io.onedev.commons.utils.match.WildcardUtils;
import io.onedev.server.model.CodeComment;
import io.onedev.server.model.support.Mark;
import io.onedev.server.util.ProjectScope;
import io.onedev.server.util.criteria.Criteria;

public class PathCriteria extends Criteria<CodeComment>  {

	private static final long serialVersionUID = 1L;

	private final String value;
	
	private final int operator;
	
	public PathCriteria(String value, int operator) {
		this.value = value;
		this.operator = operator;
	}

	@Override
	public Predicate getPredicate(@Nullable ProjectScope projectScope, CriteriaQuery<?> query, From<CodeComment, CodeComment> from, CriteriaBuilder builder) {
		Path<String> attribute = CodeCommentQuery.getPath(from, CodeComment.PROP_MARK + "." + Mark.PROP_PATH);
		String normalized = value.toLowerCase().replace('*', '%');
		if (normalized.endsWith("/"))
			normalized += "%";
		var predicate = builder.like(builder.lower(attribute), normalized);
		if (operator == CodeCommentQueryLexer.IsNot)
			predicate = builder.not(predicate);
		return predicate;
	}
	
	@Override
	public boolean matches(CodeComment comment) {
		var matches = WildcardUtils.matchPath(value.toLowerCase(), comment.getMark().getPath().toLowerCase());
		if (operator == CodeCommentQueryLexer.IsNot)
			matches = !matches;
		return matches;
	}

	@Override
	public String toStringWithoutParens() {
		return quote(CodeComment.NAME_PATH) + " " 
				+ CodeCommentQuery.getRuleName(operator) + " " 
				+ quote(value);
	}

}
