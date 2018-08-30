package io.onedev.server.search.entity;

import java.io.Serializable;

import javax.persistence.criteria.Predicate;

import io.onedev.server.model.AbstractEntity;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;

public abstract class EntityCriteria<T extends AbstractEntity> implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	public abstract Predicate getPredicate(Project project, QueryBuildContext<T> context, User user);

	public abstract boolean matches(T entity, User user);
	
	public abstract boolean needsLogin();
	
	@Override
	public abstract String toString();
	
}
