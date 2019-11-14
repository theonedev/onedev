package io.onedev.server.search.entity;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import io.onedev.server.model.AbstractEntity;
import io.onedev.server.model.User;

public class NotCriteriaHelper<T extends AbstractEntity> extends EntityCriteria<T> {
	
	private static final long serialVersionUID = 1L;

	private final EntityCriteria<T> criteria;
	
	public NotCriteriaHelper(EntityCriteria<T> criteria) {
		this.criteria = criteria;
	}

	@Override
	public Predicate getPredicate(Root<T> root, CriteriaBuilder builder, User user) {
		return criteria.getPredicate(root, builder, user).not();
	}

	@Override
	public boolean matches(T entity, User user) {
		return !criteria.matches(entity, user);
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
