package io.onedev.server.search.buildmetric;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import io.onedev.server.model.Build;

public class JobCriteria extends BuildMetricCriteria {

	private static final long serialVersionUID = 1L;

	private String jobName;
	
	public JobCriteria(String jobName) {
		this.jobName = jobName;
	}

	@Override
	public Predicate getPredicate(Root<?> metricRoot, Join<?, ?> buildJoin, CriteriaBuilder builder) {
		Path<String> attribute = buildJoin.get(Build.PROP_JOB);
		String normalized = jobName.toLowerCase().replace("*", "%");
		return builder.like(builder.lower(attribute), normalized);
	}

	@Override
	public String toStringWithoutParens() {
		return quote(Build.NAME_JOB) + " " 
				+ BuildMetricQuery.getRuleName(BuildMetricQueryLexer.Is) + " " 
				+ quote(jobName);
	}

}
