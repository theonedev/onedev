package io.onedev.server.util;

import java.io.Serializable;

import javax.annotation.Nullable;

import org.eclipse.jgit.lib.ObjectId;

import io.onedev.server.model.Project;

public class ProjectAwareCommitId implements Serializable {

	private static final long serialVersionUID = 1L;

	private final Project project;
	
	private final ObjectId commitId;
	
	public ProjectAwareCommitId(Project project, ObjectId commitId) {
		this.project = project;
		this.commitId = commitId;
	}

	public Project getProject() {
		return project;
	}

	public ObjectId getCommitId() {
		return commitId;
	}

	@Nullable
	public static ProjectAwareCommitId from(String revisionFQN) {
		ProjectAwareRevision revision = ProjectAwareRevision.from(revisionFQN);
		if (revision != null) {
			Project project = revision.getProject();
			return new ProjectAwareCommitId(project, project.getObjectId(revision.getRevision(), false));
		} else {
			return null;
		}
	}
	
}
