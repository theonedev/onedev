package io.onedev.server.rest.resource.support;

import java.io.Serializable;

import io.onedev.server.rest.annotation.Api;
import jakarta.validation.constraints.NotBlank;

public class CompareContextData implements Serializable {

	private static final long serialVersionUID = 1L;

	@Api(description="Full old commit hash, or all zeros for an empty repository state")
	private String oldCommitHash;

	@Api(description="Full new commit hash")
	private String newCommitHash;

	@Api(description="Optional pull request id")
	private Long pullRequestId;

	@NotBlank
	public String getOldCommitHash() {
		return oldCommitHash;
	}

	public void setOldCommitHash(String oldCommitHash) {
		this.oldCommitHash = oldCommitHash;
	}

	@NotBlank
	public String getNewCommitHash() {
		return newCommitHash;
	}

	public void setNewCommitHash(String newCommitHash) {
		this.newCommitHash = newCommitHash;
	}

	public Long getPullRequestId() {
		return pullRequestId;
	}

	public void setPullRequestId(Long pullRequestId) {
		this.pullRequestId = pullRequestId;
	}

}
