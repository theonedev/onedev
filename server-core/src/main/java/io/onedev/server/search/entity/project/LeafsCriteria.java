package io.onedev.server.search.entity.project;

import org.jspecify.annotations.Nullable;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

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
