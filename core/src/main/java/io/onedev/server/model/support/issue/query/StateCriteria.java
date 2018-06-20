package io.onedev.server.model.support.issue.query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;

public class StateCriteria extends IssueCriteria {

	private static final long serialVersionUID = 1L;

	private String value;
	
	private int operator;
	
	public StateCriteria(String value, int operator) {
		this.value = value;
		this.operator = operator;
	}

	@Override
	public Predicate getPredicate(Project project, QueryBuildContext context) {
		Path<?> attribute = context.getRoot().get(Issue.BUILTIN_FIELDS.get(Issue.STATE));
		if (operator == IssueQueryLexer.Is)
			return context.getBuilder().equal(attribute, value);
		else
			return context.getBuilder().notEqual(attribute, value);
	}

	@Override
	public boolean matches(Issue issue) {
		if (operator == IssueQueryLexer.Is)
			return issue.getState().equals(value);
		else
			return !issue.getState().equals(value);
	}

	@Override
	public boolean needsLogin() {
		return false;
	}

	@Override
	public void fill(Issue issue, Set<String> initedLists) {
		if (operator == IssueQueryLexer.Is)
			issue.setState(value);
	}

	@Override
	public String toString() {
		return IssueQuery.quote(Issue.STATE) + " " + IssueQuery.getRuleName(operator) + " " + IssueQuery.quote(value);
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
