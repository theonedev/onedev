package io.onedev.server.model.support.issue.query;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.Predicate;

import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.inputspec.InputSpec;
import io.onedev.server.util.inputspec.userchoiceinput.UserChoiceInput;
import io.onedev.server.util.query.QueryBuildContext;

public class MineCriteria extends IssueCriteria {

	private static final long serialVersionUID = 1L;

	private IssueCriteria getCriteria(Project project) {
		IssueCriteria submitterCriteria = new SubmittedByCriteria(SecurityUtils.getUser());
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
	public Predicate getPredicate(Project project, QueryBuildContext<Issue> context) {
		return getCriteria(project).getPredicate(project, context);
	}

	@Override
	public boolean matches(Issue issue) {
		return getCriteria(issue.getProject()).matches(issue);
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
