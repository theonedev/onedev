package io.onedev.server.search.buildmetric;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import org.eclipse.jgit.lib.Constants;

import io.onedev.server.model.Build;

public class BranchCriteria extends BuildMetricCriteria {

	private static final long serialVersionUID = 1L;

	private final String value;
	
	private final int operator;
	
	public BranchCriteria(String value, int operator) {
		this.value = value;
		this.operator = operator;
	}

	@Override
	public Predicate getPredicate(Root<?> metricRoot, Join<?, ?> buildJoin, CriteriaBuilder builder) {
		Path<String> attribute = buildJoin.get(Build.PROP_REF_NAME);
		String normalized = Constants.R_HEADS + value.toLowerCase().replace("*", "%");
		var predicate = builder.like(builder.lower(attribute), normalized);
		if (operator == BuildMetricQueryLexer.IsNot)
			predicate = builder.not(predicate);
		return predicate;
	}

	@Override
	public String toStringWithoutParens() {
		return quote(Build.NAME_BRANCH) + " " 
				+ BuildMetricQuery.getRuleName(operator) + " " 
				+ quote(value);
	}

}
