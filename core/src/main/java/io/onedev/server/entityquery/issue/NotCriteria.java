package io.onedev.server.entityquery.issue;

import javax.persistence.criteria.Predicate;

import io.onedev.server.entityquery.NotCriteriaHelper;
import io.onedev.server.entityquery.QueryBuildContext;
import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;

public class NotCriteria extends IssueCriteria {
	
	private static final long serialVersionUID = 1L;

	private final IssueCriteria criteria;
	
	public NotCriteria(IssueCriteria criteria) {
		this.criteria = criteria;
	}

	@Override
	public Predicate getPredicate(Project project, QueryBuildContext<Issue> context) {
		return new NotCriteriaHelper<Issue>(criteria).getPredicate(project, context);
	}

	@Override
	public boolean matches(Issue issue) {
		return new NotCriteriaHelper<Issue>(criteria).matches(issue);
	}

	@Override
	public boolean needsLogin() {
		return new NotCriteriaHelper<Issue>(criteria).needsLogin();
	}

	@Override
	public String toString() {
		return new NotCriteriaHelper<Issue>(criteria).toString();
	}
	
}
