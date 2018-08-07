package io.onedev.server.entityquery.issue;

import javax.annotation.Nullable;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;

import io.onedev.server.entityquery.QueryBuildContext;
import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.issue.IssueConstants;
import io.onedev.server.entityquery.issue.IssueQueryLexer;

public class DescriptionCriteria extends IssueCriteria {

	private static final long serialVersionUID = 1L;

	private final String value;
	
	public DescriptionCriteria(@Nullable String value) {
		this.value = value;
	}

	@Override
	public Predicate getPredicate(Project project, QueryBuildContext<Issue> context) {
		Expression<String> attribute = context.getRoot().get(IssueConstants.ATTR_DESCRIPTION);
		if (value != null)
			return context.getBuilder().like(context.getBuilder().lower(attribute), "%" + value.toLowerCase() + "%");
		else
			return context.getBuilder().isNull(attribute);
	}

	@Override
	public boolean matches(Issue issue) {
		if (value != null)
			return issue.getDescription() != null && issue.getDescription().toLowerCase().contains(value.toLowerCase());
		else
			return issue.getDescription() == null;
	}

	@Override
	public boolean needsLogin() {
		return false;
	}

	@Override
	public String toString() {
		if (value != null)
			return IssueQuery.quote(IssueConstants.FIELD_DESCRIPTION) + " " + IssueQuery.getRuleName(IssueQueryLexer.Contains) + " " + IssueQuery.quote(value);
		else
			return IssueQuery.quote(IssueConstants.FIELD_DESCRIPTION) + " " + IssueQuery.getRuleName(IssueQueryLexer.IsEmpty);
	}

}
