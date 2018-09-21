package io.onedev.server.search.entity.issue;

import java.util.Set;

import javax.annotation.Nullable;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import io.onedev.server.model.Issue;
import io.onedev.server.model.Milestone;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.model.support.issue.IssueConstants;
import io.onedev.server.search.entity.QueryBuildContext;

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
	public Predicate getPredicate(Project project, QueryBuildContext<Issue> context, User user) {
		Path<?> attribute = context.getJoin(IssueConstants.FIELD_MILESTONE).get(Milestone.FIELD_ATTR_NAME);
		if (value != null)
			return context.getBuilder().equal(attribute, value);
		else
			return context.getBuilder().isNull(attribute);
	}

	@Override
	public boolean matches(Issue issue, User user) {
		if (value != null)
			return issue.getMilestone() != null && issue.getMilestone().getName().equals(value);
		else
			return issue.getMilestone() == null;
	}

	@Override
	public boolean needsLogin() {
		return false;
	}

	@Override
	public String toString() {
		if (value != null) 
			return IssueQuery.quote(IssueConstants.FIELD_MILESTONE) + " " + IssueQuery.getRuleName(IssueQueryLexer.Is) + " " + IssueQuery.quote(value);
		else
			return IssueQuery.quote(IssueConstants.FIELD_MILESTONE) + " " + IssueQuery.getRuleName(IssueQueryLexer.IsEmpty);
	}

	@Override
	public void fill(Issue issue, Set<String> initedLists) {
		if (value != null)
			issue.setMilestone(issue.getProject().getMilestone(value));
	}

}
