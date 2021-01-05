package io.onedev.server.search.buildmetric;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.eclipse.jgit.lib.Constants;

import io.onedev.server.model.Build;

public class BranchCriteria extends BuildMetricCriteria {

	private static final long serialVersionUID = 1L;

	private final String value;
	
	public BranchCriteria(String value) {
		this.value = value;
	}

	@Override
	public Predicate getPredicate(Root<?> metricRoot, Join<?, ?> buildJoin, CriteriaBuilder builder) {
		Path<String> attribute = buildJoin.get(Build.PROP_REF_NAME);
		String normalized = Constants.R_HEADS + value.toLowerCase().replace("*", "%");
		return builder.like(builder.lower(attribute), normalized);
	}

	@Override
	public String toStringWithoutParens() {
		return quote(Build.NAME_BRANCH) + " " 
				+ BuildMetricQuery.getRuleName(BuildMetricQueryLexer.Is) + " " 
				+ quote(value);
	}

}
