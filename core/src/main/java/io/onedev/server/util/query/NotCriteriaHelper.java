package io.onedev.server.util.query;

import javax.persistence.criteria.Predicate;

import io.onedev.server.model.AbstractEntity;
import io.onedev.server.model.Project;

public class NotCriteriaHelper<T extends AbstractEntity> extends EntityCriteria<T> {
	
	private static final long serialVersionUID = 1L;

	private final EntityCriteria<T> criteria;
	
	public NotCriteriaHelper(EntityCriteria<T> criteria) {
		this.criteria = criteria;
	}

	@Override
	public Predicate getPredicate(Project project, QueryBuildContext<T> context) {
		return criteria.getPredicate(project, context).not();
	}

	@Override
	public boolean matches(T entity) {
		return !criteria.matches(entity);
	}

	@Override
	public boolean needsLogin() {
		return criteria.needsLogin();
	}

	@Override
	public String toString() {
		return "not(" + criteria.toString() + ")";
	}
	
}
