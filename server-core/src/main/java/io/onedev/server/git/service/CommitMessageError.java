package io.onedev.server.git.service;

import io.onedev.server.git.GitUtils;
import org.eclipse.jgit.lib.ObjectId;

import java.io.Serializable;

import static io.onedev.server.git.GitUtils.abbreviateSHA;

public class CommitMessageError implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private final ObjectId commitId;
	
	private final String errorMessage;
	
	public CommitMessageError(ObjectId commitId, String errorMessage) {
		this.commitId = commitId;
		this.errorMessage = errorMessage;
	}

	public ObjectId getCommitId() {
		return commitId;
	}

	public String getErrorMessage() {
		return errorMessage;
	}
	
	@Override
	public String toString() {
		return "Error validating commit message of " 
				+ abbreviateSHA(commitId.name()) 
				+ ": " + errorMessage;
	}
}
