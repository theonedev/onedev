package io.onedev.server.search.entity.codecomment;

import java.util.Date;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import io.onedev.server.model.CodeComment;
import io.onedev.server.model.support.LastActivity;
import io.onedev.server.util.criteria.Criteria;

public class LastActivityDateCriteria extends Criteria<CodeComment>  {

	private static final long serialVersionUID = 1L;

	private final int operator;
	
	private final Date value;
	
	private final String rawValue;
	
	public LastActivityDateCriteria(Date value, String rawValue, int operator) {
		this.operator = operator;
		this.value = value;
		this.rawValue = rawValue;
	}

	@Override
	public Predicate getPredicate(CriteriaQuery<?> query, From<CodeComment, CodeComment> from, CriteriaBuilder builder) {
		Path<Date> attribute = CodeCommentQuery.getPath(from, CodeComment.PROP_LAST_ACTIVITY + "." + LastActivity.PROP_DATE);
		if (operator == CodeCommentQueryLexer.IsUntil)
			return builder.lessThan(attribute, value);
		else
			return builder.greaterThan(attribute, value);
	}

	@Override
	public boolean matches(CodeComment comment) {
		if (operator == CodeCommentQueryLexer.IsUntil)
			return comment.getLastActivity().getDate().before(value);
		else
			return comment.getLastActivity().getDate().after(value);
	}

	@Override
	public String toStringWithoutParens() {
		return quote(CodeComment.NAME_LAST_ACTIVITY_DATE) + " " 
				+ CodeCommentQuery.getRuleName(operator) + " " 
				+ quote(rawValue);
	}

}
