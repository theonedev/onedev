package io.onedev.server.search.entity.workspace;

import org.jspecify.annotations.Nullable;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;

import io.onedev.server.model.Workspace;
import io.onedev.server.util.ProjectScope;
import io.onedev.server.util.criteria.Criteria;

public abstract class StatusCriteria extends Criteria<Workspace> {

	private static final long serialVersionUID = 1L;

	public abstract Workspace.Status getStatus();

	@Override
	public Predicate getPredicate(@Nullable ProjectScope projectScope, CriteriaQuery<?> query, From<Workspace, Workspace> from, CriteriaBuilder builder) {
		Path<Workspace.Status> attribute = from.get(Workspace.PROP_STATUS);
		return builder.equal(attribute, getStatus());
	}

	@Override
	public boolean matches(Workspace workspace) {
		return workspace.getStatus() == getStatus();
	}

}
