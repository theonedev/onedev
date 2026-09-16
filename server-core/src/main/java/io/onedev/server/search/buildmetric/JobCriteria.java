package io.onedev.server.search.buildmetric;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import io.onedev.server.model.Build;

public class JobCriteria extends BuildMetricCriteria {

	private static final long serialVersionUID = 1L;

	private final String jobName;
	
	private final int operator;
	
	public JobCriteria(String jobName, int operator) {
		this.jobName = jobName;
		this.operator = operator;
	}

	@Override
	public Predicate getPredicate(Root<?> metricRoot, Join<?, ?> buildJoin, CriteriaBuilder builder) {
		Path<String> attribute = buildJoin.get(Build.PROP_JOB_NAME);
		String normalized = jobName.toLowerCase().replace("*", "%");
		var predicate = builder.like(builder.lower(attribute), normalized);
		if (operator == BuildMetricQueryLexer.IsNot)
			predicate = builder.not(predicate);
		return predicate;
	}

	@Override
	public String toStringWithoutParens() {
		return quote(Build.NAME_JOB) + " " 
				+ BuildMetricQuery.getRuleName(operator) + " " 
				+ quote(jobName);
	}

}
