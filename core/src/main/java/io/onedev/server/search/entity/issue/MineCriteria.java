package io.onedev.server.search.entity.issue;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.Predicate;

import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.search.entity.QueryBuildContext;
import io.onedev.server.util.inputspec.InputSpec;
import io.onedev.server.util.inputspec.userchoiceinput.UserChoiceInput;

public class MineCriteria extends IssueCriteria {

	private static final long serialVersionUID = 1L;

	private IssueCriteria getCriteria(Project project, User user) {
		IssueCriteria submitterCriteria = new SubmittedByCriteria(user);
		List<IssueCriteria> fieldCriterias = new ArrayList<>();
		for (InputSpec field: project.getIssueWorkflow().getFieldSpecs()) {
			if (field instanceof UserChoiceInput) {
				IssueCriteria fieldCriteria = new FieldOperatorCriteria(field.getName(), IssueQueryLexer.IsMe);
				fieldCriterias.add(fieldCriteria);
			} 
		}
		if (!fieldCriterias.isEmpty()) {
			fieldCriterias.add(0, submitterCriteria);
			return new OrCriteria(fieldCriterias);
		} else {
			return submitterCriteria;
		}
	}
	
	@Override
	public Predicate getPredicate(Project project, QueryBuildContext<Issue> context, User user) {
		return getCriteria(project, user).getPredicate(project, context, user);
	}

	@Override
	public boolean matches(Issue issue, User user) {
		return getCriteria(issue.getProject(), user).matches(issue, user);
	}

	@Override
	public boolean needsLogin() {
		return true;
	}

	@Override
	public String toString() {
		return IssueQuery.getRuleName(IssueQueryLexer.Mine);
	}

}
