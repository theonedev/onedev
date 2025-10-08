package io.onedev.server.search.entity.issue;

import java.util.Objects;

import org.jspecify.annotations.Nullable;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Predicate;

import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueField;
import io.onedev.server.model.Project;
import io.onedev.server.search.entity.EntityQuery;

public class IssueFieldCriteria extends FieldCriteria {

	private static final long serialVersionUID = 1L;

	private final Project project;
	
	private final String value;
	
	private final int operator;
	
	private transient Issue issue;
	
	public IssueFieldCriteria(String name, @Nullable Project project, String value, int operator) {
		super(name);
		this.project = project;
		this.value = value;
		this.operator = operator;
	}
	
	private Issue getIssue() {
		if (issue == null)
			issue = EntityQuery.getIssue(project, value);
		return issue;
	}

	@Override
	protected Predicate getValuePredicate(From<Issue, Issue> issueFrom, From<IssueField, IssueField> fieldFrom, 
			CriteriaBuilder builder) {
		var predicate = builder.equal(fieldFrom.get(IssueField.PROP_ORDINAL), getIssue().getId());
		if (operator == IssueQueryLexer.IsNot)
			predicate = builder.not(predicate);
		return predicate;
	}

	@Override
	public boolean matches(Issue issue) {
		Object fieldValue = issue.getFieldValue(getFieldName());
		var matches = issue.getProject().equals(getIssue().getProject()) && Objects.equals(fieldValue, getIssue().getId());
		if (operator == IssueQueryLexer.IsNot)
			matches = !matches;
		return matches;
	}

	@Override
	public void fill(Issue issue) {
		if (operator == IssueQueryLexer.Is)
			issue.setFieldValue(getFieldName(), getIssue().getId());
	}
	
	@Override
	public String toStringWithoutParens() {
		return quote(getFieldName()) + " " 
				+ IssueQuery.getRuleName(operator) + " " 
				+ quote(value);
	}

}
