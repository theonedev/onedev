package io.onedev.server.search.entity.issue;

import javax.annotation.Nullable;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;
import io.onedev.server.search.entity.EntityQuery;
import io.onedev.server.util.ProjectScopedNumber;
import io.onedev.server.util.criteria.Criteria;

public class NumberCriteria extends Criteria<Issue> {

	private static final long serialVersionUID = 1L;

	private final Project project;
	
	private final int operator;
	
	private final String value;
	
	private transient ProjectScopedNumber number;
	
	public NumberCriteria(@Nullable Project project, String value, int operator) {
		this.project = project;
		this.operator = operator;
		this.value = value;
	}

	private ProjectScopedNumber getNumber() {
		if (number == null) 
			number = EntityQuery.getProjectScopedNumber(project, value);
		return number;
	}
	
	@Override
	public Predicate getPredicate(CriteriaQuery<?> query, From<Issue, Issue> from, CriteriaBuilder builder) {
		Path<Long> attribute = from.get(Issue.PROP_NUMBER);
		Predicate numberPredicate;
		
		if (operator == IssueQueryLexer.Is)
			numberPredicate = builder.equal(attribute, getNumber().getNumber());
		else if (operator == IssueQueryLexer.IsGreaterThan)
			numberPredicate = builder.greaterThan(attribute, getNumber().getNumber());
		else
			numberPredicate = builder.lessThan(attribute, getNumber().getNumber());
		
		return builder.and(
				builder.equal(from.get(Issue.PROP_PROJECT), getNumber().getProject()),
				numberPredicate);
	}

	@Override
	public boolean matches(Issue issue) {
		if (issue.getProject().equals(getNumber().getProject())) {
			if (operator == IssueQueryLexer.Is)
				return issue.getNumber() == getNumber().getNumber();
			else if (operator == IssueQueryLexer.IsGreaterThan)
				return issue.getNumber() > getNumber().getNumber();
			else
				return issue.getNumber() < getNumber().getNumber();
		} else {
			return false;
		}
	}

	@Override
	public String toStringWithoutParens() {
		return quote(Issue.NAME_NUMBER) + " " 
				+ IssueQuery.getRuleName(operator) + " " 
				+ quote(value);
	}

}
