package io.onedev.server.search.entity.issue;

import org.jspecify.annotations.Nullable;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;
import io.onedev.server.util.ProjectScope;
import io.onedev.server.util.criteria.Criteria;

public class ProjectIsCurrentCriteria extends Criteria<Issue> {

	private static final long serialVersionUID = 1L;
	
	@Override
	public Predicate getPredicate(@Nullable ProjectScope projectScope, CriteriaQuery<?> query, From<Issue, Issue> from, CriteriaBuilder builder) {
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
