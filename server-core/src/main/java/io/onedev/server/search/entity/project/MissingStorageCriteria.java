package io.onedev.server.search.entity.project;

import org.jspecify.annotations.Nullable;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Predicate;

import io.onedev.server.OneDev;
import io.onedev.server.service.ProjectService;
import io.onedev.server.model.Project;
import io.onedev.server.util.ProjectScope;
import io.onedev.server.util.criteria.Criteria;

public class MissingStorageCriteria extends Criteria<Project> {

	private static final long serialVersionUID = 1L;

	@Override
	public Predicate getPredicate(@Nullable ProjectScope projectScope, CriteriaQuery<?> query, From<Project, Project> from, CriteriaBuilder builder) {
		ProjectService projectService = OneDev.getInstance(ProjectService.class);
		return forManyValues(builder, from.get(Project.PROP_ID),
				projectService.getIdsMissingStorage(), projectService.getIds());
	}

	@Override
	public boolean matches(Project project) {
		return OneDev.getInstance(ProjectService.class).isMissingStorage(project.getId());
	}

	@Override
	public String toStringWithoutParens() {
		return ProjectQuery.getRuleName(ProjectQueryLexer.MissingStorage);
	}

}
