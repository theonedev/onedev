package io.onedev.server.model.support.issue.query;

import java.io.Serializable;

import javax.persistence.criteria.Predicate;

public abstract class IssueCriteria implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	public abstract Predicate getPredicate(QueryBuildContext context);

}
