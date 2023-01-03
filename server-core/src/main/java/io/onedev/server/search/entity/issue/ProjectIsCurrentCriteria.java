package io.onedev.server.search.entity.issue;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;
import io.onedev.server.util.criteria.Criteria;

import javax.persistence.criteria.*;

public class ProjectIsCurrentCriteria extends Criteria<Issue> {

	private static final long serialVersionUID = 1L;
	
	@Override
	public Predicate getPredicate(CriteriaQuery<?> query, From<Issue, Issue> from, CriteriaBuilder builder) {
		if (Project.get() != null) 
			return builder.equal(from.join(Issue.PROP_PROJECT, JoinType.INNER), Project.get());
		else
			throw new ExplicitException("No project in query context");
	}

	@Override
	public boolean matches(Issue issue) {
		if (Project.get() != null)
			return Project.get().equals(issue.getProject());
		else
			throw new ExplicitException("No project in query context");
	}

	@Override
	public String toStringWithoutParens() {
		return quote(Issue.NAME_PROJECT) + " "
				+ IssueQuery.getRuleName(IssueQueryLexer.IsCurrent);
	}

}
