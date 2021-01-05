package io.onedev.server.search.buildmetric;

import java.io.Serializable;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import io.onedev.commons.utils.StringUtils;

public abstract class BuildMetricCriteria implements Serializable {

	private static final long serialVersionUID = 1L;

	private boolean withParens;
	
	public BuildMetricCriteria withParens(boolean withParens) {
		this.withParens = withParens;
		return this;
	}

	public abstract Predicate getPredicate(Root<?> metrixRoot, Join<?, ?> buildJoin, CriteriaBuilder builder);
	
	public static String quote(String value) {
		return "\"" + StringUtils.escape(value, "\"") + "\"";
	}

	@Override
	public String toString() {
		if (withParens)
			return "(" + toStringWithoutParens() + ")";
		else
			return toStringWithoutParens();
	}
	
	public abstract String toStringWithoutParens();
	
}
