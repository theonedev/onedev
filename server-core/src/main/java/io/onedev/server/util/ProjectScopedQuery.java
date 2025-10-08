package io.onedev.server.util;

import io.onedev.server.OneDev;
import io.onedev.server.service.ProjectService;
import io.onedev.server.model.Project;

import org.jspecify.annotations.Nullable;

public class ProjectScopedQuery {
	
	private final Project project;
	
	private final String query;
	
	public ProjectScopedQuery(Project project, String query) {
		this.project = project;
		this.query = query;
	}

	public Project getProject() {
		return project;
	}

	public String getQuery() {
		return query;
	}

	@Nullable
	public static ProjectScopedQuery of(@Nullable Project currentProject, String query, 
										@Nullable Character pathIndicator, 
										@Nullable Character keyIndicator) {
		if (pathIndicator != null) {
			var index = query.indexOf(pathIndicator);
			if (index != -1) {
				var projectPath = query.substring(0, index);
				var newQuery = query.substring(index + 1);
				if (projectPath.length() != 0) {
					currentProject = getProjectService().findByPath(projectPath);
					if (currentProject != null)
						return new ProjectScopedQuery(currentProject, newQuery);
					else
						return null;
				} else if (currentProject != null) {
					return new ProjectScopedQuery(currentProject, newQuery);
				} else {
					return null;
				}
			}
		}
		if (keyIndicator != null) {
			var index = query.indexOf(keyIndicator);
			if (index != -1) {
				currentProject = getProjectService().findByKey(query.substring(0, index));
				if (currentProject != null)
					return new ProjectScopedQuery(currentProject, query.substring(index+1));
				else
					return null;
			}
		}
		if (currentProject != null)
			return new ProjectScopedQuery(currentProject, query);
		else
			return null;
	}
	
	private static ProjectService getProjectService() {
		return OneDev.getInstance(ProjectService.class);
	}
	
}
