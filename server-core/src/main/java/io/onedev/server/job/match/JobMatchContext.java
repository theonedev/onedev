package io.onedev.server.job.match;

import org.jspecify.annotations.Nullable;

import org.eclipse.jgit.lib.ObjectId;

import io.onedev.server.model.Project;

public class JobMatchContext {

	private final Project project;
	
	@Nullable
	private final String branch;
	
	@Nullable
	private final ObjectId commitId;
		
	@Nullable
	private final String jobName;

	public JobMatchContext(Project project, @Nullable String branch, @Nullable ObjectId commitId, @Nullable String jobName) {
		this.project = project;
		this.branch = branch;
		this.commitId = commitId;
		this.jobName = jobName;
	}

	public Project getProject() {
		return project;
	}

	@Nullable
	public String getBranch() {
		return branch;
	}

	@Nullable
	public ObjectId getCommitId() {
		return commitId;
	}
	
	@Nullable
	public String getJobName() {
		return jobName;
	}

}
