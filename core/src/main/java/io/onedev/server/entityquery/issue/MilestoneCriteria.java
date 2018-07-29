package io.onedev.server.entityquery.issue;

import java.util.Set;

import javax.annotation.Nullable;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import io.onedev.server.entityquery.QueryBuildContext;
import io.onedev.server.model.Issue;
import io.onedev.server.model.Milestone;
import io.onedev.server.model.Project;
import io.onedev.server.entityquery.issue.IssueQueryLexer;

public class MilestoneCriteria extends IssueCriteria {

	private static final long serialVersionUID = 1L;
	
	private final String value;

	public MilestoneCriteria(@Nullable String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	@Override
	public Predicate getPredicate(Project project, QueryBuildContext<Issue> context) {
		Path<?> attribute = context.getJoin(Issue.FIELD_MILESTONE).get(Milestone.NAME);
		if (value != null)
			return context.getBuilder().equal(attribute, value);
		else
			return context.getBuilder().isNull(attribute);
	}

	@Override
	public boolean matches(Issue issue) {
		return issue.getMilestone() != null && issue.getMilestone().getName().equals(value);
	}

	@Override
	public boolean needsLogin() {
		return false;
	}

	@Override
	public String toString() {
		if (value != null) 
			return IssueQuery.quote(Issue.FIELD_MILESTONE) + " " + IssueQuery.getRuleName(IssueQueryLexer.Is) + " " + IssueQuery.quote(value);
		else
			return IssueQuery.quote(Issue.FIELD_MILESTONE) + " " + IssueQuery.getRuleName(IssueQueryLexer.IsEmpty);
	}

	@Override
	public void fill(Issue issue, Set<String> initedLists) {
		if (value != null)
			issue.setMilestone(issue.getProject().getMilestone(value));
	}

}
