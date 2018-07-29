package io.onedev.server.entityquery.issue;

import java.util.Objects;
import java.util.Set;

import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import io.onedev.server.entityquery.QueryBuildContext;
import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueFieldUnary;
import io.onedev.server.model.Project;
import io.onedev.server.entityquery.issue.IssueQueryLexer;

public class BooleanFieldCriteria extends FieldCriteria {

	private static final long serialVersionUID = 1L;

	private final boolean value;
	
	public BooleanFieldCriteria(String name, boolean value) {
		super(name);
		this.value = value;
	}

	@Override
	public Predicate getPredicate(Project project, QueryBuildContext<Issue> context) {
		Path<String> attribute = context.getJoin(getFieldName()).get(IssueFieldUnary.VALUE);
		return context.getBuilder().equal(attribute, String.valueOf(value));
	}

	@Override
	public boolean matches(Issue issue) {
		return Objects.equals(value, issue.getFieldValue(getFieldName()));
	}

	@Override
	public boolean needsLogin() {
		return false;
	}

	@Override
	public String toString() {
		return IssueQuery.quote(getFieldName()) + " " + IssueQuery.getRuleName(IssueQueryLexer.Is) + " " + IssueQuery.quote(String.valueOf(value));
	}

	@Override
	public void fill(Issue issue, Set<String> initedLists) {
		issue.setFieldValue(getFieldName(), value);
	}

}
