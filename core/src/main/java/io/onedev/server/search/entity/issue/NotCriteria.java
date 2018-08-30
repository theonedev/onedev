package io.onedev.server.search.entity.issue;

import javax.persistence.criteria.Predicate;

import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.search.entity.NotCriteriaHelper;
import io.onedev.server.search.entity.QueryBuildContext;

public class NotCriteria extends IssueCriteria {
	
	private static final long serialVersionUID = 1L;

	private final IssueCriteria criteria;
	
	public NotCriteria(IssueCriteria criteria) {
		this.criteria = criteria;
	}

	@Override
	public Predicate getPredicate(Project project, QueryBuildContext<Issue> context, User user) {
		return new NotCriteriaHelper<Issue>(criteria).getPredicate(project, context, user);
	}

	@Override
	public boolean matches(Issue issue, User user) {
		return new NotCriteriaHelper<Issue>(criteria).matches(issue, user);
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
