package io.onedev.server.search.entity.issue;

import java.util.Date;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;

import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueField;
import io.onedev.server.model.User;
import io.onedev.server.search.entity.EntityQuery;

public class DateFieldCriteria extends FieldCriteria {

	private static final long serialVersionUID = 1L;

	private final Date date;
	
	private final String value;
	
	private final int operator;
	
	public DateFieldCriteria(String name, String value, int operator) {
		super(name);
		date = EntityQuery.getDateValue(value);
		this.value = value;
		this.operator = operator;
	}

	@Override
	protected Predicate getValuePredicate(Join<?, ?> field, CriteriaBuilder builder, User user) {
		if (operator == IssueQueryLexer.IsBefore)
			return builder.lessThan(field.get(IssueField.ATTR_ORDINAL), date.getTime());
		else
			return builder.greaterThan(field.get(IssueField.ATTR_ORDINAL), date.getTime());
	}

	@Override
	public boolean matches(Issue issue, User user) {
		Date fieldValue = (Date) issue.getFieldValue(getFieldName());
		if (operator == IssueQueryLexer.IsBefore)
			return fieldValue != null && fieldValue.before(date);
		else
			return fieldValue != null && fieldValue.after(date);
	}

	@Override
	public boolean needsLogin() {
		return false;
	}

	@Override
	public String toString() {
		return IssueQuery.quote(getFieldName()) + " " + IssueQuery.getRuleName(operator) 
			+ " " + IssueQuery.quote(value);
	}

}
