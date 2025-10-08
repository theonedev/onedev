package io.onedev.server.search.entity.pullrequest;

import org.jspecify.annotations.Nullable;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import io.onedev.server.entityreference.PullRequestReference;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.util.ProjectScope;
import io.onedev.server.util.criteria.Criteria;

public class ReferenceCriteria extends Criteria<PullRequest> {

	private static final long serialVersionUID = 1L;

	private final int operator;
	
	private final String value;
	
	private final PullRequestReference reference;
	
	public ReferenceCriteria(@Nullable Project project, String value, int operator) {
		this.operator = operator;
		this.value = value;
		reference = PullRequestReference.of(value, project);
	}

	@Override
	public Predicate getPredicate(@Nullable ProjectScope projectScope, CriteriaQuery<?> query, From<PullRequest, PullRequest> from, CriteriaBuilder builder) {
		Path<Long> attribute = from.get(PullRequest.PROP_NUMBER);
		Predicate numberPredicate;
		
		if (operator == PullRequestQueryLexer.Is)
			numberPredicate = builder.equal(attribute, reference.getNumber());
		else if (operator == PullRequestQueryLexer.IsNot)
			numberPredicate = builder.not(builder.equal(attribute, reference.getNumber()));
		else if (operator == PullRequestQueryLexer.IsGreaterThan)
			numberPredicate = builder.greaterThan(attribute, reference.getNumber());
		else
			numberPredicate = builder.lessThan(attribute, reference.getNumber());
		
		return builder.and(
				builder.equal(from.get(PullRequest.PROP_TARGET_PROJECT), reference.getProject()),
				numberPredicate);
	}

	@Override
	public boolean matches(PullRequest request) {
		if (request.getTargetProject().equals(reference.getProject())) {
			if (operator == PullRequestQueryLexer.Is)
				return request.getNumber() == reference.getNumber();
			else if (operator == PullRequestQueryLexer.IsNot)
				return request.getNumber() != reference.getNumber();
			else if (operator == PullRequestQueryLexer.IsGreaterThan)
				return request.getNumber() > reference.getNumber();
			else
				return request.getNumber() < reference.getNumber();
		} else {
			return false;
		}
	}

	@Override
	public String toStringWithoutParens() {
		return quote(PullRequest.NAME_NUMBER) + " " 
				+ PullRequestQuery.getRuleName(operator) + " " 
				+ quote(value);
	}

}
