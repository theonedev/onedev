package io.onedev.server.model.support.issue.query;

import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;
import io.onedev.server.util.query.QueryBuildContext;

public class TitleCriteria extends IssueCriteria {

	private static final long serialVersionUID = 1L;

	private final String value;
	
	public TitleCriteria(String value) {
		this.value = value;
	}

	@Override
	public Predicate getPredicate(Project project, QueryBuildContext<Issue> context) {
		Path<String> attribute = context.getRoot().get(Issue.FIELD_PATHS.get(Issue.FIELD_TITLE));
		return context.getBuilder().like(attribute, "%" + value + "%");
	}

	@Override
	public boolean matches(Issue issue) {
		return issue.getTitle().toLowerCase().contains(value);
	}

	@Override
	public boolean needsLogin() {
		return false;
	}

	@Override
	public String toString() {
		return IssueQuery.quote(Issue.FIELD_TITLE) + " " + IssueQuery.getRuleName(IssueQueryLexer.Is) + " " + IssueQuery.quote(value);
	}

}
