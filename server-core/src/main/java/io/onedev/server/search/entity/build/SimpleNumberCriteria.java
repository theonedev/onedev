package io.onedev.server.search.entity.build;

import io.onedev.server.model.Build;
import io.onedev.server.util.criteria.Criteria;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Predicate;

public class SimpleNumberCriteria extends Criteria<Build> {

	private static final long serialVersionUID = 1L;
	
	private final long value;
	
	public SimpleNumberCriteria(long value) {
		this.value = value;
	}
	
	@Override
	public Predicate getPredicate(CriteriaQuery<?> query, From<Build, Build> from, CriteriaBuilder builder) {
		return builder.equal(from.get(Build.PROP_NUMBER), value);
	}

	@Override
	public boolean matches(Build build) {
		return build.getNumber() == value;
	}

	@Override
	public String toStringWithoutParens() {
		return "#" + value;
	}

}
