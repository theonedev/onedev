package io.onedev.server.search.entity.codecomment;

import java.util.Date;

import org.jspecify.annotations.Nullable;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import io.onedev.server.model.CodeComment;
import io.onedev.server.model.support.LastActivity;
import io.onedev.server.util.ProjectScope;
import io.onedev.server.util.criteria.Criteria;

public class LastActivityDateCriteria extends Criteria<CodeComment>  {

	private static final long serialVersionUID = 1L;

	private final int operator;
	
	private final Date date;
	
	private final String value;
	
	public LastActivityDateCriteria(Date date, String value, int operator) {
		this.operator = operator;
		this.date = date;
		this.value = value;
	}
	
	public int getOperator() {
		return operator;
	}

	public Date getDate() {
		return date;
	}

	@Override
	public Predicate getPredicate(@Nullable ProjectScope projectScope, CriteriaQuery<?> query, From<CodeComment, CodeComment> from, CriteriaBuilder builder) {
		Path<Date> attribute = CodeCommentQuery.getPath(from, CodeComment.PROP_LAST_ACTIVITY + "." + LastActivity.PROP_DATE);
		if (operator == CodeCommentQueryLexer.IsUntil)
			return builder.lessThan(attribute, date);
		else
			return builder.greaterThan(attribute, date);
	}

	@Override
	public boolean matches(CodeComment comment) {
		if (operator == CodeCommentQueryLexer.IsUntil)
			return comment.getLastActivity().getDate().before(date);
		else
			return comment.getLastActivity().getDate().after(date);
	}

	@Override
	public String toStringWithoutParens() {
		return quote(CodeComment.NAME_LAST_ACTIVITY_DATE) + " " 
				+ CodeCommentQuery.getRuleName(operator) + " " 
				+ quote(value);
	}

}
