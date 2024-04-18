package io.onedev.server.search.entity.project;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.model.Project;
import io.onedev.server.util.criteria.Criteria;

import javax.persistence.criteria.*;

public class PendingDeleteCriteria extends Criteria<Project> {

	private static final long serialVersionUID = 1L;

	@Override
	public Predicate getPredicate(CriteriaQuery<?> query, From<Project, Project> from, CriteriaBuilder builder) {
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
