package io.onedev.server.search.entity.issue;

import java.util.Date;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Predicate;

import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueField;
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
	protected Predicate getValuePredicate(From<Issue, Issue> issueFrom, From<IssueField, IssueField> fieldFrom, CriteriaBuilder builder) {
		if (operator == IssueQueryLexer.IsUntil)
			return builder.lessThan(fieldFrom.get(IssueField.PROP_ORDINAL), date.getTime());
		else
			return builder.greaterThan(fieldFrom.get(IssueField.PROP_ORDINAL), date.getTime());
	}

	@Override
	public boolean matches(Issue issue) {
		Date fieldValue = (Date) issue.getFieldValue(getFieldName());
		if (operator == IssueQueryLexer.IsUntil)
			return fieldValue != null && fieldValue.before(date);
		else
			return fieldValue != null && fieldValue.after(date);
	}

	@Override
	public String toStringWithoutParens() {
		return quote(getFieldName()) + " " 
				+ IssueQuery.getRuleName(operator) + " " 
				+ quote(value);
	}

}
