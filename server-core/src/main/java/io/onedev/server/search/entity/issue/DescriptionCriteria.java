package io.onedev.server.search.entity.issue;

import javax.annotation.Nullable;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.util.IssueConstants;

public class DescriptionCriteria extends IssueCriteria {

	private static final long serialVersionUID = 1L;

	private final String value;
	
	public DescriptionCriteria(@Nullable String value) {
		this.value = value;
	}

	@Override
	public Predicate getPredicate(Project project, Root<Issue> root, CriteriaBuilder builder, User user) {
		Expression<String> attribute = root.get(IssueConstants.ATTR_DESCRIPTION);
		if (value != null)
			return builder.like(builder.lower(attribute), "%" + value.toLowerCase() + "%");
		else
			return builder.isNull(attribute);
	}

	@Override
	public boolean matches(Issue issue, User user) {
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
