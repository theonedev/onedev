package io.onedev.server.util.script.identity;

import javax.annotation.Nullable;

import org.eclipse.jgit.lib.ObjectId;

import io.onedev.server.model.Project;

public class JobIdentity implements ScriptIdentity {

	private final Project project;
	
	private final ObjectId commitId;
	
	public JobIdentity(Project project, @Nullable ObjectId commitId) {
		this.project = project;
		this.commitId = commitId;
	}
	
	public Project getProject() {
		return project;
	}
	
	@Nullable
	public ObjectId getCommitId() {
		return commitId;
	}
	
}
