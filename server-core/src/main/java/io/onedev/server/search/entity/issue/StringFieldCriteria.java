package io.onedev.server.search.entity.issue;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueField;


public class StringFieldCriteria extends FieldCriteria {

	private static final long serialVersionUID = 1L;

	private final String value;
	
	private final int operator;
	
	public StringFieldCriteria(String name, String value, int operator) {
		super(name);
		this.value = value;
		this.operator = operator;
	}

	public String getValue() {
		return value;
	}

	@Override
	protected Predicate getValuePredicate(Join<?, ?> field, CriteriaBuilder builder) {
		Path<String> attribute = field.get(IssueField.PROP_VALUE);
		if (operator == IssueQueryLexer.Is)
			return builder.equal(builder.lower(attribute), value.toLowerCase());
		else 
			return builder.like(builder.lower(attribute), "%" + value.toLowerCase() + "%");
	}

	@Override
	public boolean matches(Issue issue) {
		String fieldValue = (String) issue.getFieldValue(getFieldName());
		if (operator == IssueQueryLexer.Is)
			return value.equalsIgnoreCase(fieldValue);
		else 
			return fieldValue != null && fieldValue.toLowerCase().contains(value.toLowerCase());
	}

	@Override
	public String toStringWithoutParens() {
		return quote(getFieldName()) + " " 
				+ IssueQuery.getRuleName(operator) + " " 
				+ quote(value);
	}

	@Override
	public void fill(Issue issue) {
		if (operator == IssueQueryLexer.Is)
			issue.setFieldValue(getFieldName(), value);
	}
	
}
