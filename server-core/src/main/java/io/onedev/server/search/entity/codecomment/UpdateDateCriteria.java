package io.onedev.server.search.entity.codecomment;

import java.util.Date;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import io.onedev.server.model.CodeComment;

import io.onedev.server.search.entity.EntityCriteria;
import io.onedev.server.util.query.CodeCommentQueryConstants;

public class UpdateDateCriteria extends EntityCriteria<CodeComment>  {

	private static final long serialVersionUID = 1L;

	private final int operator;
	
	private final Date value;
	
	private final String rawValue;
	
	public UpdateDateCriteria(Date value, String rawValue, int operator) {
		this.operator = operator;
		this.value = value;
		this.rawValue = rawValue;
	}

	@Override
	public Predicate getPredicate(Root<CodeComment> root, CriteriaBuilder builder) {
		Path<Date> attribute = CodeCommentQuery.getPath(root, CodeCommentQueryConstants.ATTR_UPDATE_DATE);
		if (operator == CodeCommentQueryLexer.IsBefore)
			return builder.lessThan(attribute, value);
		else
			return builder.greaterThan(attribute, value);
	}

	@Override
	public boolean matches(CodeComment comment) {
		if (operator == CodeCommentQueryLexer.IsBefore)
			return comment.getUpdateDate().before(value);
		else
			return comment.getUpdateDate().after(value);
	}

	@Override
	public String toString() {
		return CodeCommentQuery.quote(CodeCommentQueryConstants.FIELD_UPDATE_DATE) + " " 
				+ CodeCommentQuery.getRuleName(operator) + " " 
				+ CodeCommentQuery.quote(rawValue);
	}

}
