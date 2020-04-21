package io.onedev.server.search.entity;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import io.onedev.server.model.AbstractEntity;
import io.onedev.server.util.criteria.AndCriteria;

public class AndEntityCriteria<T extends AbstractEntity> extends EntityCriteria<T> {
	
	private static final long serialVersionUID = 1L;
	
	private final List<? extends EntityCriteria<T>> criterias;

	public AndEntityCriteria(List<? extends EntityCriteria<T>> criterias) {
		this.criterias = criterias;
	}

	@Override
	public void onRenameUser(String oldName, String newName) {
		new AndCriteria<T>(criterias).onRenameUser(oldName, newName);
	}

	@Override
	public void onRenameProject(String oldName, String newName) {
		new AndCriteria<T>(criterias).onRenameProject(oldName, newName);
	}

	@Override
	public void onRenameGroup(String oldName, String newName) {
		new AndCriteria<T>(criterias).onRenameGroup(oldName, newName);
	}

	@Override
	public boolean isUsingUser(String userName) {
		return new AndCriteria<T>(criterias).isUsingUser(userName);
	}

	@Override
	public boolean isUsingProject(String projectName) {
		return new AndCriteria<T>(criterias).isUsingProject(projectName);
	}

	@Override
	public boolean isUsingGroup(String groupName) {
		return new AndCriteria<T>(criterias).isUsingGroup(groupName);
	}
	
	@Override
	public Predicate getPredicate(Root<T> root, CriteriaBuilder builder) {
		List<Predicate> predicates = new ArrayList<>();
		for (EntityCriteria<T> criteria: criterias)
			predicates.add(criteria.getPredicate(root, builder));
		return builder.and(predicates.toArray(new Predicate[0]));
	}

	@Override
	public String toStringWithoutParens() {
		return new AndCriteria<T>(criterias).toStringWithoutParens();
	}

	@Override
	public boolean matches(T t) {
		return new AndCriteria<T>(criterias).matches(t);
	}

}
