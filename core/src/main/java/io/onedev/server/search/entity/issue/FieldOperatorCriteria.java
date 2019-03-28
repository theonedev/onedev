package io.onedev.server.search.entity.issue;

import java.util.Objects;
import java.util.Set;

import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueFieldEntity;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.search.entity.QueryBuildContext;
import io.onedev.server.security.SecurityUtils;

public class FieldOperatorCriteria extends FieldCriteria {

	private static final long serialVersionUID = 1L;
	
	private final int operator;

	public FieldOperatorCriteria(String name, int operator) {
		super(name);
		this.operator = operator;
	}

	@Override
	public Predicate getPredicate(Project project, QueryBuildContext<Issue> context, User user) {
		Path<?> attribute = context.getJoin(getFieldName()).get(IssueFieldEntity.FIELD_ATTR_VALUE);
		if (operator == IssueQueryLexer.IsEmpty)
			return context.getBuilder().isNull(attribute);
		else
			return context.getBuilder().equal(attribute, user.getName());
	}

	@Override
	public boolean matches(Issue issue, User user) {
		Object fieldValue = issue.getFieldValue(getFieldName());
		if (operator == IssueQueryLexer.IsEmpty)
			return fieldValue == null;
		else 
			return Objects.equals(fieldValue, user.getName());
	}

	@Override
	public boolean needsLogin() {
		return operator == IssueQueryLexer.IsMe;
	}

	@Override
	public String toString() {
		return IssueQuery.quote(getFieldName()) + " " + IssueQuery.getRuleName(operator);
	}

	@Override
	public void fill(Issue issue, Set<String> initedLists) {
		if (operator == IssueQueryLexer.IsEmpty)
			issue.setFieldValue(getFieldName(), null);
		else if (operator == IssueQueryLexer.IsMe)
			issue.setFieldValue(getFieldName(), SecurityUtils.getUser().getName());
	}

}
