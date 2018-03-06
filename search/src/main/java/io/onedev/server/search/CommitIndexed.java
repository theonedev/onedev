package io.onedev.server.search;

import org.eclipse.jgit.lib.ObjectId;

import io.onedev.server.model.Project;

public class CommitIndexed {
	
	private final Project project;
	
	private final ObjectId commitId;
	
	public CommitIndexed(Project project, ObjectId commitId) {
		this.project = project;
		this.commitId = commitId;
	}

	public Project getProject() {
		return project;
	}

	public ObjectId getCommitId() {
		return commitId;
	}
	
}
