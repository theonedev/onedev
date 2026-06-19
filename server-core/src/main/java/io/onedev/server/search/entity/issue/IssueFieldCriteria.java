package io.onedev.server.search.entity.issue;

import java.util.Objects;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Predicate;

import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueField;

public class IssueFieldCriteria extends FieldCriteria {

	private static final long serialVersionUID = 1L;

	private final Long number;
	
	private final int operator;
		
	public IssueFieldCriteria(String name, Long number, int operator) {
		super(name);
		this.number = number;
		this.operator = operator;
	}
	
	@Override
	protected Predicate getValuePredicate(From<Issue, Issue> issueFrom, From<IssueField, IssueField> fieldFrom, 
			CriteriaBuilder builder) {
		var predicate = builder.equal(fieldFrom.get(IssueField.PROP_ORDINAL), number);
		if (operator == IssueQueryLexer.IsNot)
			predicate = builder.not(predicate);
		return predicate;
	}

	@Override
	public boolean matches(Issue issue) {
		Object fieldValue = issue.getFieldValue(getFieldName());
		var matches = Objects.equals(fieldValue, number);
		if (operator == IssueQueryLexer.IsNot)
			matches = !matches;
		return matches;
	}

	@Override
	public void fill(Issue issue) {
		if (operator == IssueQueryLexer.Is)
			issue.setFieldValue(getFieldName(), number);
	}
	
	@Override
	public String toStringWithoutParens() {
		return quote(getFieldName()) + " " 
				+ IssueQuery.getRuleName(operator) + " " 
				+ quote(String.valueOf(number));
	}

}
