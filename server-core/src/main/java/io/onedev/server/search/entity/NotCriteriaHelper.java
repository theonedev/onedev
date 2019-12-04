package io.onedev.server.search.entity;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import io.onedev.server.model.AbstractEntity;

public class NotCriteriaHelper<T extends AbstractEntity> extends EntityCriteria<T> {
	
	private static final long serialVersionUID = 1L;

	private final EntityCriteria<T> criteria;
	
	public NotCriteriaHelper(EntityCriteria<T> criteria) {
		this.criteria = criteria;
	}

	@Override
	public Predicate getPredicate(Root<T> root, CriteriaBuilder builder) {
		return criteria.getPredicate(root, builder).not();
	}

	@Override
	public boolean matches(T entity) {
		return !criteria.matches(entity);
	}

	@Override
	public String toString() {
		return "not(" + criteria.toString() + ")";
	}
	
}
