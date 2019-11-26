package io.onedev.server.search.entity.project;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.search.entity.EntityCriteria;
import io.onedev.server.search.entity.NotCriteriaHelper;

public class NotCriteria extends EntityCriteria<Project> {
	
	private static final long serialVersionUID = 1L;

	private final EntityCriteria<Project> criteria;
	
	public NotCriteria(EntityCriteria<Project> criteria) {
		this.criteria = criteria;
	}

	@Override
	public Predicate getPredicate(Root<Project> root, CriteriaBuilder builder, User user) {
		return new NotCriteriaHelper<Project>(criteria).getPredicate(root, builder, user);
	}

	@Override
	public boolean matches(Project project, User user) {
		return new NotCriteriaHelper<Project>(criteria).matches(project, user);
	}

	@Override
	public boolean needsLogin() {
		return new NotCriteriaHelper<Project>(criteria).needsLogin();
	}

	@Override
	public String toString() {
		return new NotCriteriaHelper<Project>(criteria).toString();
	}
		
}
