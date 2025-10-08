package io.onedev.server.search.entity.issue;

import org.jspecify.annotations.Nullable;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import io.onedev.server.entityreference.IssueReference;
import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;
import io.onedev.server.util.ProjectScope;
import io.onedev.server.util.criteria.Criteria;

public class ReferenceCriteria extends Criteria<Issue> {

	private static final long serialVersionUID = 1L;
	
	private final int operator;
	
	private final String value;
	
	private final IssueReference reference;
	
	public ReferenceCriteria(@Nullable Project project, String value, int operator) {
		this.operator = operator;
		this.value = value;
		reference = IssueReference.of(value, project);
	}
	
	@Override
	public Predicate getPredicate(@Nullable ProjectScope projectScope, CriteriaQuery<?> query, From<Issue, Issue> from, CriteriaBuilder builder) {
		Path<Long> attribute = from.get(Issue.PROP_NUMBER);
		Predicate numberPredicate;
		
		if (operator == IssueQueryLexer.Is)
			numberPredicate = builder.equal(attribute, reference.getNumber());
		else if (operator == IssueQueryLexer.IsNot)
			numberPredicate = builder.not(builder.equal(attribute, reference.getNumber()));			
		else if (operator == IssueQueryLexer.IsGreaterThan)
			numberPredicate = builder.greaterThan(attribute, reference.getNumber());
		else
			numberPredicate = builder.lessThan(attribute, reference.getNumber());
		
		return builder.and(
				builder.equal(from.get(Issue.PROP_PROJECT), reference.getProject()),
				numberPredicate);
	}

	@Override
	public boolean matches(Issue issue) {
		if (issue.getProject().equals(reference.getProject())) {
			if (operator == IssueQueryLexer.Is)
				return issue.getNumber() == reference.getNumber();
			else if (operator == IssueQueryLexer.IsNot)
				return issue.getNumber() != reference.getNumber();				
			else if (operator == IssueQueryLexer.IsGreaterThan)
				return issue.getNumber() > reference.getNumber();
			else
				return issue.getNumber() < reference.getNumber();
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
