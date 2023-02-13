package io.onedev.server.util;

import io.onedev.commons.utils.StringUtils;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.model.Project;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.annotation.Nullable;
import java.io.Serializable;

public class ProjectScopedRevision implements Serializable {

	private static final long serialVersionUID = 1L;

	private final Long projectId;
	
	private final String revision;
	
	public ProjectScopedRevision(Project project, String revision) {
		this(project.getId(), revision);
	}

	public ProjectScopedRevision(Long projectId, String revision) {
		this.projectId = projectId;
		this.revision = revision;
	}
	
	public Project getProject() {
		return getProjectManager().load(projectId);
	}

	public String getRevision() {
		return revision;
	}

	private static ProjectManager getProjectManager() {
		return OneDev.getInstance(ProjectManager.class);
	}
	
	@Nullable
	public static ProjectScopedRevision from(String revisionFQN) {
		String projectName = StringUtils.substringBefore(revisionFQN, ":");
		String revision = StringUtils.substringAfter(revisionFQN, ":");
		Project project = getProjectManager().findByPath(projectName);
		if (project != null)
			return new ProjectScopedRevision(project, revision);
		else
			return null;
	}

	public String getFQN() {
		return getProject().getPath() + ":" + revision;
	}

	@Override
	public String toString() {
		return getFQN();
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof ProjectScopedRevision))
			return false;
		if (this == other)
			return true;
		ProjectScopedRevision projectScopedRevision = (ProjectScopedRevision) other;
		return new EqualsBuilder()
				.append(projectId, projectScopedRevision.projectId)
				.append(revision, projectScopedRevision.revision)
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37).append(projectId).append(revision).toHashCode();
	}
	
}
