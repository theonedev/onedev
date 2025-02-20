package io.onedev.server.search.entity.project;

import javax.annotation.Nullable;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import io.onedev.server.model.Project;
import io.onedev.server.util.ProjectScope;
import io.onedev.server.util.criteria.Criteria;

public class PendingDeleteCriteria extends Criteria<Project> {

	private static final long serialVersionUID = 1L;

	@Override
	public Predicate getPredicate(@Nullable ProjectScope projectScope, CriteriaQuery<?> query, From<Project, Project> from, CriteriaBuilder builder) {
		Path<String> attribute = from.get(Project.PROP_PENDING_DELETE);
		return builder.equal(attribute, true);
	}

	@Override
	public boolean matches(Project project) {
		return project.isPendingDelete();
	}

	@Override
	public String toStringWithoutParens() {
		return ProjectQuery.getRuleName(ProjectQueryLexer.PendingDelete);
	}

}
