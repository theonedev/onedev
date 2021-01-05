package io.onedev.server.search.entity.codecomment;

import java.util.Date;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import io.onedev.server.model.CodeComment;
import io.onedev.server.search.entity.EntityCriteria;

public class CreateDateCriteria extends EntityCriteria<CodeComment> {

	private static final long serialVersionUID = 1L;

	private final int operator;
	
	private final Date value;
	
	private final String rawValue;
	
	public CreateDateCriteria(Date value, String rawValue, int operator) {
		this.operator = operator;
		this.value = value;
		this.rawValue = rawValue;
	}

	@Override
	public Predicate getPredicate(Root<CodeComment> root, CriteriaBuilder builder) {
		Path<Date> attribute = root.get(CodeComment.PROP_CREATE_DATE);
		if (operator == CodeCommentQueryLexer.IsUntil)
			return builder.lessThan(attribute, value);
		else
			return builder.greaterThan(attribute, value);
	}

	@Override
	public boolean matches(CodeComment comment) {
		if (operator == CodeCommentQueryLexer.IsUntil)
			return comment.getCreateDate().before(value);
		else
			return comment.getCreateDate().after(value);
	}

	@Override
	public String toStringWithoutParens() {
		return quote(CodeComment.NAME_CREATE_DATE) + " " 
				+ CodeCommentQuery.getRuleName(operator) + " " 
				+ quote(rawValue);
	}

}
