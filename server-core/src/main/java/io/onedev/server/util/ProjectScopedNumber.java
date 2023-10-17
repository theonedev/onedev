package io.onedev.server.util;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.commons.utils.StringUtils;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.model.Project;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serializable;

public class ProjectScopedNumber implements Serializable {

	private static final long serialVersionUID = 1L;

	private final Long projectId; 
	
	private final Long number;

	public ProjectScopedNumber(Project project, Long number) {
		this(project.getId(), number);
	}
	
	public ProjectScopedNumber(Long projectId, Long number) {
		this.projectId = projectId;
		this.number = number;
	}
	
	private static ProjectManager getProjectManager() {
		return OneDev.getInstance(ProjectManager.class);
	}
	
	public static ProjectScopedNumber from(String fqn) {
		if (fqn.contains("#")) {
			String projectName = StringUtils.substringBefore(fqn, "#");
			if (projectName.length() != 0) {
				Project project = getProjectManager().findByPath(projectName);
				if (project != null) {
					String numberStr = StringUtils.substringAfter(fqn, "#");
					try {
						return new ProjectScopedNumber(project, Long.valueOf(numberStr));
					} catch (NumberFormatException e) {
						throw new ExplicitException("Invalid number: " + numberStr);
					}
				} else {
					throw new ExplicitException("Unable to find project: " + projectName);
				}
			} 
		}
		throw new ExplicitException("Project is not specified");
	}

	public Project getProject() {
		return getProjectManager().load(projectId);
	}
	
	public Long getProjectId() {
		return projectId;
	}

	public Long getNumber() {
		return number;
	}
	
	@Override
	public boolean equals(Object other) {
		if (!(other instanceof ProjectScopedNumber))
			return false;
		if (this == other)
			return true;
		ProjectScopedNumber otherProjectScopedNumber = (ProjectScopedNumber) other;
		return new EqualsBuilder()
				.append(projectId, otherProjectScopedNumber.projectId)
				.append(number, otherProjectScopedNumber.number)
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37).append(projectId).append(number).toHashCode();
	}
	
	@Override
	public String toString() {
		return getProject().getPath() + "#" + getNumber();
	}
	
}
