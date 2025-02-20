package io.onedev.server.util;

import java.util.Collection;

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
	
}
