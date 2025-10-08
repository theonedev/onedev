package io.onedev.server.git.service;

import static io.onedev.server.git.GitUtils.abbreviateSHA;

import java.io.Serializable;

import org.jspecify.annotations.Nullable;

import org.eclipse.jgit.lib.ObjectId;

public class CommitMessageError implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private final ObjectId commitId;
	
	private final String errorMessage;
	
	public CommitMessageError(@Nullable ObjectId commitId, String errorMessage) {
		this.commitId = commitId;
		this.errorMessage = errorMessage;
	}

	@Nullable
	public ObjectId getCommitId() {
		return commitId;
	}

	public String getErrorMessage() {
		return errorMessage;
	}
	
	@Override
	public String toString() {
		if (commitId != null) {
			return "Error validating commit message of "
					+ abbreviateSHA(commitId.name())
					+ ": " + errorMessage;
		} else {
			return "Error validating auto merge commit message: " + errorMessage;
		}
	}
}
