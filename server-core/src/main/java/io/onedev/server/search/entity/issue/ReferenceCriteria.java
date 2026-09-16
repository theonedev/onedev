package io.onedev.server.search.entity.issue;

import org.jspecify.annotations.Nullable;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;

import io.onedev.server.entityreference.IssueReference;
import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;
import io.onedev.server.util.ProjectScope;
import io.onedev.server.util.criteria.Criteria;

public class ReferenceCriteria extends Criteria<Issue> {

	private static final long serialVersionUID = 1L;
	
	private final int operator;
	
	private final Project project;
	
	private final IssueReference reference;
	
	public ReferenceCriteria(@Nullable Project project, String value, int operator) {
		this.operator = operator;
		this.project = project;
		reference = IssueReference.of(value, project);
	}
	
	@Override
	public Predicate getPredicate(@Nullable ProjectScope projectScope, CriteriaQuery<?> query, From<Issue, Issue> from, CriteriaBuilder builder) {
		Path<Long> attribute = from.get(Issue.PROP_NUMBER);
		Predicate numberPredicate;
		
		if (operator == IssueQueryLexer.Is)
			numberPredicate = builder.equal(attribute, reference.getNumber());
		else 
			numberPredicate = builder.not(builder.equal(attribute, reference.getNumber()));
		
		return builder.and(
				builder.equal(from.get(Issue.PROP_PROJECT), reference.getProject()),
				numberPredicate);
	}

	@Override
	public boolean matches(Issue issue) {
		if (issue.getProject().equals(reference.getProject())) {
			if (operator == IssueQueryLexer.Is)
				return issue.getNumber() == reference.getNumber();
			else 
				return issue.getNumber() != reference.getNumber();
		} else {
			return false;
		}
	}

	@Override
	public String toStringWithoutParens() {
		return reference.toString(project);
	}

}
