package io.onedev.server.search.entity.issue;

import java.util.Objects;

import javax.annotation.Nullable;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Predicate;

import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueField;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.search.entity.EntityQuery;

public class PullRequestFieldCriteria extends FieldCriteria {

	private static final long serialVersionUID = 1L;

	private final Project project;
	
	private final String value;
	
	private transient PullRequest request;
	
	public PullRequestFieldCriteria(String name, @Nullable Project project, String value) {
		super(name);
		this.project = project;
		this.value = value;
	}
	
	private PullRequest getRequest() {
		if (request == null)
			request = EntityQuery.getPullRequest(project, value);
		return request;
	}

	@Override
	protected Predicate getValuePredicate(From<Issue, Issue> issueFrom, From<IssueField, IssueField> fieldFrom, 
			CriteriaBuilder builder) {
		return builder.equal(fieldFrom.get(IssueField.PROP_ORDINAL), getRequest().getId());
	}

	@Override
	public boolean matches(Issue issue) {
		Object fieldValue = issue.getFieldValue(getFieldName());
		return issue.getProject().equals(getRequest().getTargetProject()) 
				&& Objects.equals(fieldValue, getRequest().getId());
	}

	@Override
	public void fill(Issue issue) {
		issue.setFieldValue(getFieldName(), getRequest().getId());
	}
	
	@Override
	public String toStringWithoutParens() {
		return quote(getFieldName()) + " " 
				+ IssueQuery.getRuleName(IssueQueryLexer.Is) + " " 
				+ quote(value);
	}

}
