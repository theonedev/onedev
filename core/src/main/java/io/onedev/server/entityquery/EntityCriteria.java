package io.onedev.server.entityquery;

import java.io.Serializable;

import javax.persistence.criteria.Predicate;

import io.onedev.server.model.AbstractEntity;
import io.onedev.server.model.Project;

public abstract class EntityCriteria<T extends AbstractEntity> implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	public abstract Predicate getPredicate(Project project, QueryBuildContext<T> context);

	public abstract boolean matches(T entity);
	
	public abstract boolean needsLogin();
	
	@Override
	public abstract String toString();
	
}
