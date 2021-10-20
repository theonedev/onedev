package io.onedev.server.search.entity.project;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.model.Project;
import io.onedev.server.search.entity.EntityCriteria;
import io.onedev.server.util.criteria.Criteria;

public class DescendentsOfCriteria extends EntityCriteria<Project> {

	private static final long serialVersionUID = 1L;

	private final Project project;
	
	public DescendentsOfCriteria(Project project) {
		this.project = project;
	}

	@Override
	public Predicate getPredicate(Root<Project> root, CriteriaBuilder builder) {
		return builder.and(
				OneDev.getInstance(ProjectManager.class).getTreePredicate(builder, root, project),
				builder.notEqual(root, project));
	}

	@Override
	public boolean matches(Project project) {
		return !this.project.equals(project) && this.project.isSelfOrAncestorOf(project);
	}

	@Override
	public String toStringWithoutParens() {
		return ProjectQuery.getRuleName(ProjectQueryLexer.DescendentsOf) + " " + Criteria.quote(project.getPath());
	}

}
