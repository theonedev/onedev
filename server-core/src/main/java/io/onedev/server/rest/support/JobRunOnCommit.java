package io.onedev.server.rest.support;

import javax.validation.constraints.NotNull;

import javax.validation.constraints.NotEmpty;

import io.onedev.server.model.Build;
import io.onedev.server.rest.annotation.Api;
import io.onedev.server.rest.annotation.EntityCreate;
import io.onedev.server.util.validation.annotation.CommitHash;

@EntityCreate(Build.class)
public class JobRunOnCommit extends JobRun {
	
	private static final long serialVersionUID = 1L;

	@Api(order=100)
	private Long projectId; 
	
	@Api(order=200)
	private String commitHash;

	@Api(order=300, description="Git ref to run build against, for instance <i>refs/heads/main</i>. "
			+ "Specified commit must be reachable from the ref")
	private String refName;
	
	@NotNull
	public Long getProjectId() {
		return projectId;
	}

	public void setProjectId(Long projectId) {
		this.projectId = projectId;
	}

	@CommitHash
	@NotEmpty
	public String getCommitHash() {
		return commitHash;
	}

	public void setCommitHash(String commitHash) {
		this.commitHash = commitHash;
	}

	@NotEmpty
	public String getRefName() {
		return refName;
	}

	public void setRefName(String refName) {
		this.refName = refName;
	}

}