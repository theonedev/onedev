package io.onedev.server.util;

import io.onedev.commons.utils.StringUtils;
import io.onedev.server.OneDev;
import io.onedev.server.OneException;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.model.Project;

public class ProjectScopedNumber {

	private final Project project; 
	
	private final Long number;

	public ProjectScopedNumber(Project project, Long number) {
		this.project = project;
		this.number = number;
	}
	
	public static ProjectScopedNumber from(String fqn) {
		if (fqn.contains("#")) {
			String projectName = StringUtils.substringBefore(fqn, "#");
			if (projectName.length() != 0) {
				Project project = OneDev.getInstance(ProjectManager.class).find(projectName);
				if (project != null) {
					String numberStr = StringUtils.substringAfter(fqn, "#");
					try {
						return new ProjectScopedNumber(project, Long.valueOf(numberStr));
					} catch (NumberFormatException e) {
						throw new OneException("Invalid number: " + numberStr);
					}
				} else {
					throw new OneException("Unable to find project: " + projectName);
				}
			} 
		}
		throw new OneException("Project is not specified");
	}

	public Project getProject() {
		return project;
	}

	public Long getNumber() {
		return number;
	}
	
}
