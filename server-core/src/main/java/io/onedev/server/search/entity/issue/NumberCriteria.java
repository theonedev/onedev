package io.onedev.server.search.entity.issue;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.util.IssueConstants;

public class NumberCriteria extends IssueCriteria {

	private static final long serialVersionUID = 1L;

	private final int operator;
	
	private final long value;
	
	public NumberCriteria(long value, int operator) {
		this.operator = operator;
		this.value = value;
	}

	@Override
	public Predicate getPredicate(Project project, Root<Issue> root, CriteriaBuilder builder, User user) {
		Path<Long> attribute = root.get(IssueConstants.ATTR_NUMBER);
		if (operator == IssueQueryLexer.Is)
			return builder.equal(attribute, value);
		else if (operator == IssueQueryLexer.IsGreaterThan)
			return builder.greaterThan(attribute, value);
		else
			return builder.lessThan(attribute, value);
	}

	@Override
	public boolean matches(Issue issue, User user) {
		if (operator == IssueQueryLexer.Is)
			return issue.getNumber() == value;
		else if (operator == IssueQueryLexer.IsGreaterThan)
			return issue.getNumber() > value;
		else
			return issue.getNumber() < value;
	}

	@Override
	public boolean needsLogin() {
		return false;
	}

	@Override
	public String toString() {
		return IssueQuery.quote(IssueConstants.FIELD_NUMBER) + " " + IssueQuery.getRuleName(operator) + " " + IssueQuery.quote(String.valueOf(value));
	}

}
