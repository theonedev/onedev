package io.onedev.server.search.entity.build;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import com.hazelcast.internal.monitor.impl.GlobalPerIndexStats;
import io.onedev.server.model.Build;
import io.onedev.server.util.criteria.Criteria;

public class StatusCriteria extends Criteria<Build> {

	private static final long serialVersionUID = 1L;
	
	private final Build.Status status;
	
	private final int operator;
	
	public StatusCriteria(Build.Status status, int operator) {
		this.status = status;
		this.operator = operator;
	}

	@Override
	public Predicate getPredicate(CriteriaQuery<?> query, From<Build, Build> from, CriteriaBuilder builder) {
		var predicate = builder.equal(from.get(Build.PROP_STATUS), status);
		if (operator == BuildQueryLexer.IsNot)
			predicate = builder.not(predicate);
		return predicate;
	}

	@Override
	public boolean matches(Build build) {
		var matches = build.getStatus() == status;
		if (operator == BuildQueryLexer.IsNot)
			matches = !matches;
		return matches;
	}

	@Override
	public String toStringWithoutParens() {
		return quote(Build.NAME_STATUS) + " " 
				+ BuildQuery.getRuleName(operator) + " " 
				+ quote(status.toString());
	}

}
