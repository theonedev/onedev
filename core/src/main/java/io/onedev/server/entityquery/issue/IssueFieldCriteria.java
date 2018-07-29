package io.onedev.server.entityquery.issue;

import java.util.Objects;

import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import io.onedev.server.entityquery.QueryBuildContext;
import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueFieldUnary;
import io.onedev.server.model.Project;
import io.onedev.server.entityquery.issue.IssueQueryLexer;

public class IssueFieldCriteria extends FieldCriteria {

	private static final long serialVersionUID = 1L;

	private final long value;
	
	public IssueFieldCriteria(String name, long value) {
		super(name);
		this.value = value;
	}

	@Override
	public Predicate getPredicate(Project project, QueryBuildContext<Issue> context) {
		Path<Long> attribute = context.getJoin(getFieldName()).get(IssueFieldUnary.ORDINAL);
		return context.getBuilder().equal(attribute, value);
	}

	@Override
	public boolean matches(Issue issue) {
		Object fieldValue = issue.getFieldValue(getFieldName());
		return Objects.equals(fieldValue, value);
	}

	@Override
	public boolean needsLogin() {
		return false;
	}

	@Override
	public String toString() {
		return IssueQuery.quote(getFieldName()) + " " + IssueQuery.getRuleName(IssueQueryLexer.Is) + " " + IssueQuery.quote(String.valueOf(value));
	}

}
