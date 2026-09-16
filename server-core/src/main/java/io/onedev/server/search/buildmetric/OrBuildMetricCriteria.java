package io.onedev.server.search.buildmetric;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

public class OrBuildMetricCriteria extends BuildMetricCriteria {
	
	private static final long serialVersionUID = 1L;

	private final List<? extends BuildMetricCriteria> criterias;
	
	public OrBuildMetricCriteria(List<? extends BuildMetricCriteria> criterias) {
		this.criterias = criterias;
	}

	@Override
	public Predicate getPredicate(Root<?> metricRoot, Join<?, ?> buildJoin, CriteriaBuilder builder) {
		List<Predicate> predicates = new ArrayList<>();
		for (BuildMetricCriteria criteria: criterias)
			predicates.add(criteria.getPredicate(metricRoot, buildJoin, builder));
		return builder.or(predicates.toArray(new Predicate[0]));
	}

	@Override
	public String toStringWithoutParens() {
		return new OrBuildMetricCriteria(criterias).toStringWithoutParens();
	}
	
}
