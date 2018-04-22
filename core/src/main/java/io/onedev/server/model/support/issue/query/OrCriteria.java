package io.onedev.server.model.support.issue.query;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.Predicate;

public class OrCriteria extends IssueCriteria {
	
	private static final long serialVersionUID = 1L;

	private final List<IssueCriteria> criterias;
	
	public OrCriteria(List<IssueCriteria> criterias) {
		this.criterias = criterias;
	}

	@Override
	public Predicate getPredicate(QueryBuildContext context) {
		List<Predicate> predicates = new ArrayList<>();
		for (IssueCriteria criteria: criterias)
			predicates.add(criteria.getPredicate(context));
		return context.getBuilder().or(predicates.toArray(new Predicate[0]));
	}

}
