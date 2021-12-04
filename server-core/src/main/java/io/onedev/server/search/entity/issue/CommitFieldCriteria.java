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
import io.onedev.server.util.ProjectScopedCommit;

public class CommitFieldCriteria extends FieldCriteria {

	private static final long serialVersionUID = 1L;

	private final Project project;
	
	private final String value;
	
	private transient ProjectScopedCommit commit;
	
	public CommitFieldCriteria(String name, @Nullable Project project, String value) {
		super(name);
		this.project = project;
		this.value = value;
	}

	private ProjectScopedCommit getCommit() {
		if (commit == null)
			commit = EntityQuery.getCommitId(project, value);
		return commit;
	}
	
	@Override
	protected Predicate getValuePredicate(From<Issue, Issue> issueFrom, From<IssueField, IssueField> fieldFrom, CriteriaBuilder builder) {
		return builder.and(
				builder.equal(issueFrom.get(Issue.PROP_PROJECT), getCommit().getProject()),
				builder.equal(fieldFrom.get(IssueField.PROP_VALUE), getCommit().getCommitId().name()));
	}

	@Override
	public boolean matches(Issue issue) {
		Object fieldValue = issue.getFieldValue(getFieldName());
		return issue.getProject().equals(getCommit().getProject()) 
				&& Objects.equals(fieldValue, getCommit().getCommitId().name());
	}

	@Override
	public String toStringWithoutParens() {
		return quote(getFieldName()) + " " 
				+ IssueQuery.getRuleName(IssueQueryLexer.Is) + " " 
				+ quote(value);
	}

}
