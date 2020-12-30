package io.onedev.server.util;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.commons.utils.StringUtils;
import io.onedev.server.OneDev;
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
		return project;
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
				.append(project, otherProjectScopedNumber.project)
				.append(number, otherProjectScopedNumber.number)
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37).append(project).append(number).toHashCode();
	}
	
	@Override
	public String toString() {
		return getProject().getName() + "#" + getNumber();
	}
}
