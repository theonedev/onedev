package io.onedev.server.search.entity.issue;

import java.util.Objects;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Predicate;

import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueField;


public class BooleanFieldCriteria extends FieldCriteria {

	private static final long serialVersionUID = 1L;

	private final boolean value;
	
	private final int operator;
	
	public BooleanFieldCriteria(String name, boolean value, int operator) {
		super(name);
		this.value = value;
		this.operator = operator;
	}

	public boolean getValue() {
		return value;
	}

	@Override
	public Predicate getValuePredicate(From<Issue, Issue> issueFrom, From<IssueField, IssueField> fieldFrom, CriteriaBuilder builder) {
		var predicate = builder.equal(fieldFrom.get(IssueField.PROP_VALUE), String.valueOf(value));
		if (operator == IssueQueryLexer.IsNot)
			predicate = builder.not(predicate);
		return predicate;
	}

	@Override
	public boolean matches(Issue issue) {
		var matches = Objects.equals(value, issue.getFieldValue(getFieldName()));
		if (operator == IssueQueryLexer.IsNot)
			matches = !matches;
		return matches;
	}

	@Override
	public String toStringWithoutParens() {
		return quote(getFieldName()) + " " 
				+ IssueQuery.getRuleName(operator) + " " 
				+ quote(String.valueOf(value));
	}

	@Override
	public void fill(Issue issue) {
		if (operator == IssueQueryLexer.Is)
			issue.setFieldValue(getFieldName(), value);
	}

}
