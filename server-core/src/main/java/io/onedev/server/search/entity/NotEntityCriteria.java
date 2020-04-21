package io.onedev.server.search.entity;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import io.onedev.server.model.AbstractEntity;
import io.onedev.server.util.criteria.NotCriteria;

public class NotEntityCriteria<T extends AbstractEntity> extends EntityCriteria<T> {
	
	private static final long serialVersionUID = 1L;

	private final EntityCriteria<T> criteria;
	
	public NotEntityCriteria(EntityCriteria<T> criteria) {
		this.criteria = criteria;
	}

	@Override
	public Predicate getPredicate(Root<T> root, CriteriaBuilder builder) {
		return criteria.getPredicate(root, builder).not();
	}

	@Override
	public void onRenameUser(String oldName, String newName) {
		new NotCriteria<T>(criteria).onRenameUser(oldName, newName);
	}

	@Override
	public void onRenameProject(String oldName, String newName) {
		new NotCriteria<T>(criteria).onRenameProject(oldName, newName);
	}

	@Override
	public void onRenameGroup(String oldName, String newName) {
		new NotCriteria<T>(criteria).onRenameGroup(oldName, newName);
	}

	@Override
	public boolean isUsingUser(String userName) {
		return new NotCriteria<T>(criteria).isUsingUser(userName);
	}

	@Override
	public boolean isUsingProject(String projectName) {
		return new NotCriteria<T>(criteria).isUsingProject(projectName);
	}

	@Override
	public boolean isUsingGroup(String groupName) {
		return new NotCriteria<T>(criteria).isUsingGroup(groupName);
	}
	
	@Override
	public boolean matches(T t) {
		return new NotCriteria<T>(criteria).matches(t);
	}

	@Override
	public String toStringWithoutParens() {
		return new NotCriteria<T>(criteria).toStringWithoutParens();
	}
	
}
