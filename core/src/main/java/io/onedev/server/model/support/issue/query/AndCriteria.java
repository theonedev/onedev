package io.onedev.server.model.support.issue.query;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.Predicate;

import io.onedev.server.model.Issue;

public class AndCriteria extends IssueCriteria {
	
	private static final long serialVersionUID = 1L;

	private final List<IssueCriteria> criterias;
	
	public AndCriteria(List<IssueCriteria> criterias) {
		this.criterias = criterias;
	}

	@Override
	public Predicate getPredicate(QueryBuildContext context) {
		List<Predicate> predicates = new ArrayList<>();
		for (IssueCriteria criteria: criterias)
			predicates.add(criteria.getPredicate(context));
		return context.getBuilder().and(predicates.toArray(new Predicate[0]));
	}

	@Override
	public boolean matches(Issue issue) {
		for (IssueCriteria criteria: criterias) {
			if (!criteria.matches(issue))
				return false;
		}
		return true;
	}

	@Override
	public boolean needsLogin() {
		for (IssueCriteria criteria: criterias) {
			if (criteria.needsLogin())
				return true;
		}
		return false;
	}

}
