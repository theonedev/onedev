package io.onedev.server.search.entity.build;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import io.onedev.server.model.Build;
import io.onedev.server.util.criteria.Criteria;

public class StatusCriteria extends Criteria<Build> {

	private static final long serialVersionUID = 1L;
	
	private final Build.Status status;
	
	public StatusCriteria(Build.Status status) {
		this.status = status;
	}

	@Override
	public Predicate getPredicate(CriteriaQuery<?> query, From<Build, Build> from, CriteriaBuilder builder) {
		Path<?> attribute = from.get(Build.PROP_STATUS);
		return builder.equal(attribute, status);
	}

	@Override
	public boolean matches(Build build) {
		return build.getStatus() == status;
	}

	@Override
	public String toStringWithoutParens() {
		return quote(Build.NAME_STATUS) 
				+ " " 
				+ BuildQuery.getRuleName(BuildQueryLexer.Is) 
				+ " " 
				+ quote(status.toString());
	}

}
