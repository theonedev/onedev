package io.onedev.server.search.entity.codecomment;

import java.util.Date;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import io.onedev.server.model.CodeComment;
import io.onedev.server.model.support.LastUpdate;
import io.onedev.server.search.entity.EntityCriteria;

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
		Path<Date> attribute = CodeCommentQuery.getPath(root, CodeComment.PROP_LAST_UPDATE + "." + LastUpdate.PROP_DATE);
		if (operator == CodeCommentQueryLexer.IsUntil)
			return builder.lessThan(attribute, value);
		else
			return builder.greaterThan(attribute, value);
	}

	@Override
	public boolean matches(CodeComment comment) {
		if (operator == CodeCommentQueryLexer.IsUntil)
			return comment.getLastUpdate().getDate().before(value);
		else
			return comment.getLastUpdate().getDate().after(value);
	}

	@Override
	public String toStringWithoutParens() {
		return quote(CodeComment.NAME_UPDATE_DATE) + " " 
				+ CodeCommentQuery.getRuleName(operator) + " " 
				+ quote(rawValue);
	}

}
