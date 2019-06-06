package io.onedev.server.search.entity.issue;

import java.util.Date;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;

import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueField;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;

public class DateFieldCriteria extends FieldCriteria {

	private static final long serialVersionUID = 1L;

	private final Date value;
	
	private final String rawValue;
	
	private final int operator;
	
	public DateFieldCriteria(String name, Date value, String rawValue, int operator) {
		super(name);
		this.value = value;
		this.rawValue = rawValue;
		this.operator = operator;
	}

	@Override
	protected Predicate getValuePredicate(Project project, Join<?, ?> field, CriteriaBuilder builder, User user) {
		if (operator == IssueQueryLexer.IsBefore)
			return builder.lessThan(field.get(IssueField.ATTR_ORDINAL), value.getTime());
		else
			return builder.greaterThan(field.get(IssueField.ATTR_ORDINAL), value.getTime());
	}

	@Override
	public boolean matches(Issue issue, User user) {
		Date fieldValue = (Date) issue.getFieldValue(getFieldName());
		if (operator == IssueQueryLexer.IsBefore)
			return fieldValue != null && fieldValue.before(value);
		else
			return fieldValue != null && fieldValue.after(value);
	}

	@Override
	public boolean needsLogin() {
		return false;
	}

	@Override
	public String toString() {
		return IssueQuery.quote(getFieldName()) + " " + IssueQuery.getRuleName(operator) + " " + IssueQuery.quote(rawValue);
	}

}
