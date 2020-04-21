package io.onedev.server.search.entity.issue;

import java.util.Objects;

import javax.annotation.Nullable;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;

import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueField;
import io.onedev.server.model.Project;
import io.onedev.server.search.entity.EntityQuery;
import io.onedev.server.util.ProjectScopedCommit;

public class CommitFieldCriteria extends FieldCriteria {

	private static final long serialVersionUID = 1L;

	private final ProjectScopedCommit commit;
	
	private final String value;
	
	public CommitFieldCriteria(String name, @Nullable Project project, String value) {
		super(name);
		commit = EntityQuery.getCommitId(project, value);
		this.value = value;
	}

	@Override
	protected Predicate getValuePredicate(Join<?, ?> field, CriteriaBuilder builder) {
		return builder.and(
				builder.equal(field.getParent().get(Issue.PROP_PROJECT), commit.getProject()),
				builder.equal(field.get(IssueField.PROP_VALUE), commit.getCommitId().name()));
	}

	@Override
	public boolean matches(Issue issue) {
		Object fieldValue = issue.getFieldValue(getFieldName());
		return issue.getProject().equals(commit.getProject()) 
				&& Objects.equals(fieldValue, commit.getCommitId().name());
	}

	@Override
	public String toStringWithoutParens() {
		return quote(getFieldName()) + " " 
				+ IssueQuery.getRuleName(IssueQueryLexer.Is) + " " 
				+ quote(value);
	}

}
