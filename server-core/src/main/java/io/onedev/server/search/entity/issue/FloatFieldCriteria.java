package io.onedev.server.search.entity.issue;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueField;

public class FloatFieldCriteria extends FieldCriteria {

	private static final long serialVersionUID = 1L;

	private final float value;
	
	private final int operator;
	
	public FloatFieldCriteria(String name, float value, int operator) {
		super(name);
		this.value = value;
		this.operator = operator;
	}

	@Override
	protected Predicate getValuePredicate(From<Issue, Issue> issueFrom, From<IssueField, IssueField> fieldFrom, CriteriaBuilder builder) {
		Path<Float> attribute = fieldFrom.get(IssueField.PROP_ORDINAL);
		if (operator == IssueQueryLexer.IsGreaterThan)
			return builder.greaterThan(attribute, value);
		else
			return builder.lessThan(attribute, value);
	}

	@Override
	public boolean matches(Issue issue) {
		Float fieldValue = (Float) issue.getFieldValue(getFieldName());
		if (operator == IssueQueryLexer.IsGreaterThan)
			return fieldValue != null && fieldValue > value;
		else
			return fieldValue != null && fieldValue < value;
	}

	@Override
	public String toStringWithoutParens() {
		return quote(getFieldName()) + " " 
				+ IssueQuery.getRuleName(operator) + " " 
				+ quote(String.valueOf(value));
	}

	@Override
	public void fill(Issue issue) {
	}

}
