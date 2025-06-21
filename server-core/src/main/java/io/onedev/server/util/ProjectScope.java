package io.onedev.server.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Predicate;

import io.onedev.server.model.Project;
import io.onedev.server.model.support.ProjectBelonging;

public class ProjectScope {

	private final Project project;

	private final boolean inherited;
	
	private final boolean recursive;
	
	public ProjectScope(Project project, boolean inherited, boolean recursive) {
		this.project = project;
		this.inherited = inherited;
		this.recursive = recursive;
	}

	public Project getProject() {
		return project;
	}
	
	public boolean isInherited() {
		return inherited;
	}

	public boolean isRecursive() {
		return recursive;
	}
	
	public void filter(Collection<? extends ProjectBelonging> entities) {
		for (var it = entities.iterator(); it.hasNext(); ) {
			var entity = it.next();
			if (!entity.getProject().equals(project)
					&& (!inherited || !entity.getProject().isSelfOrAncestorOf(project)					
					&& (!recursive || !project.isSelfOrAncestorOf(entity.getProject())))) {
				it.remove();
			}
		}
	}
	
	public Predicate buildPredicates(CriteriaBuilder builder, From<Project, Project> root) {
		List<Predicate> predicates = new ArrayList<>();
		predicates.add(builder.equal(root, getProject()));
		if (isInherited()) {
			for (var ancestor: getProject().getAncestors()) 
				predicates.add(builder.equal(root, ancestor));
		}
		if (isRecursive()) 
			predicates.add(builder.like(root.get(Project.PROP_PATH), getProject().getPath() + "/%"));
		return builder.or(predicates.toArray(new Predicate[0]));
	}

}
