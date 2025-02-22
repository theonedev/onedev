package io.onedev.server.search.entity.issue;

import io.onedev.commons.utils.match.WildcardUtils;
import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueField;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

public class IterationFieldCriteria extends FieldCriteria {

	private static final long serialVersionUID = 1L;
	
	private final String value;
	
	private final int operator;
	
	private final boolean allowMultiple;

	public IterationFieldCriteria(String name, String value, int operator, boolean allowMultiple) {
		super(name);
		this.value = value;
		this.operator = operator;
		this.allowMultiple = allowMultiple;
	}
	
	@Override
	protected Predicate getValuePredicate(From<Issue, Issue> issueFrom, From<IssueField, IssueField> fieldFrom, CriteriaBuilder builder) {
		var predicate = builder.like(builder.lower(fieldFrom.get(IssueField.PROP_VALUE)), value.toLowerCase().replace("*", "%"));
		if (operator == IssueQueryLexer.IsNot)
			predicate = builder.not(predicate);
		return predicate;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean matches(Issue issue) {
		List<String> fieldValue = (List<String>) issue.getFieldValue(getFieldName());
		var matches = fieldValue.stream().anyMatch(it-> WildcardUtils.matchString(value.toLowerCase(), it.toLowerCase()));
		if (operator == IssueQueryLexer.IsNot)
			matches = !matches;
		return matches;
	}

	@SuppressWarnings({"unchecked" })
	@Override
	public void fill(Issue issue) {
		if (operator == IssueQueryLexer.Is) {
			if (allowMultiple) {
				List<String> valueFromIssue = (List<String>) issue.getFieldValue(getFieldName());
				if (valueFromIssue == null)
					valueFromIssue = new ArrayList<>();
				valueFromIssue.add(value);
				issue.setFieldValue(getFieldName(), valueFromIssue);
			} else {
				issue.setFieldValue(getFieldName(), value);
			}
		}
	}
	
	@Override
	public String toStringWithoutParens() {
		return quote(getFieldName()) + " " 
				+ IssueQuery.getRuleName(operator) + " " 
				+ quote(value);
	}

}
