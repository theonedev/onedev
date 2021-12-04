package io.onedev.server.search.entity.issue;

import java.util.Objects;

import javax.annotation.Nullable;
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
	
	private transient Issue issue;
	
	public IssueFieldCriteria(String name, @Nullable Project project, String value) {
		super(name);
		this.project = project;
		this.value = value;
	}
	
	private Issue getIssue() {
		if (issue == null)
			issue = EntityQuery.getIssue(project, value);
		return issue;
	}

	@Override
	protected Predicate getValuePredicate(From<Issue, Issue> issueFrom, From<IssueField, IssueField> fieldFrom, 
			CriteriaBuilder builder) {
		return builder.equal(fieldFrom.get(IssueField.PROP_ORDINAL), getIssue().getId());
	}

	@Override
	public boolean matches(Issue issue) {
		Object fieldValue = issue.getFieldValue(getFieldName());
		return issue.getProject().equals(getIssue().getProject()) && Objects.equals(fieldValue, getIssue().getId());
	}

	@Override
	public void fill(Issue issue) {
		issue.setFieldValue(getFieldName(), getIssue().getId());
	}
	
	@Override
	public String toStringWithoutParens() {
		return quote(getFieldName()) + " " 
				+ IssueQuery.getRuleName(IssueQueryLexer.Is) + " " 
				+ quote(value);
	}

}
