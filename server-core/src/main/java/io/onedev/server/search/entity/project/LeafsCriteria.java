package io.onedev.server.search.entity.project;

import org.jspecify.annotations.Nullable;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;

import io.onedev.server.model.Project;
import io.onedev.server.util.ProjectScope;
import io.onedev.server.util.criteria.Criteria;

public class LeafsCriteria extends Criteria<Project> {

	private static final long serialVersionUID = 1L;

	@Override
	public Predicate getPredicate(@Nullable ProjectScope projectScope, CriteriaQuery<?> query, From<Project, Project> from, CriteriaBuilder builder) {
		Subquery<Project> childrenQuery = query.subquery(Project.class);
		Root<Project> childrenRoot = childrenQuery.from(Project.class);
		childrenQuery.select(childrenRoot);
		
		return builder.not(builder.exists(
				childrenQuery.where(builder.equal(childrenRoot.get(Project.PROP_PARENT), from))));
	}

	@Override
	public boolean matches(Project project) {
		return project.getChildren().isEmpty();
	}

	@Override
	public String toStringWithoutParens() {
		return ProjectQuery.getRuleName(ProjectQueryLexer.Leafs);
	}

}
