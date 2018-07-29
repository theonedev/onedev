package io.onedev.server.entityquery.issue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import io.onedev.server.entityquery.QueryBuildContext;
import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;
import io.onedev.server.entityquery.issue.IssueQueryLexer;

public class StateCriteria extends IssueCriteria {

	private static final long serialVersionUID = 1L;

	private String value;
	
	public StateCriteria(String value) {
		this.value = value;
	}

	@Override
	public Predicate getPredicate(Project project, QueryBuildContext<Issue> context) {
		Path<?> attribute = context.getRoot().get(Issue.FIELD_PATHS.get(Issue.FIELD_STATE));
		return context.getBuilder().equal(attribute, value);
	}

	@Override
	public boolean matches(Issue issue) {
		return issue.getState().equals(value);
	}

	@Override
	public boolean needsLogin() {
		return false;
	}

	@Override
	public void fill(Issue issue, Set<String> initedLists) {
		issue.setState(value);
	}

	@Override
	public String toString() {
		return IssueQuery.quote(Issue.FIELD_STATE) + " " + IssueQuery.getRuleName(IssueQueryLexer.Is) + " " + IssueQuery.quote(value);
	}

	@Override
	public Collection<String> getUndefinedStates(Project project) {
		List<String> undefinedStates = new ArrayList<>();
		if (project.getIssueWorkflow().getStateSpec(value) == null)
			undefinedStates.add(value);
		return undefinedStates;
	}
	
	@Override
	public void onRenameState(String oldState, String newState) {
		if (value.equals(oldState))
			value = newState;
	}
	
}
