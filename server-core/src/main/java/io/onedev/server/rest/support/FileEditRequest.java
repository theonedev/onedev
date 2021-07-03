package io.onedev.server.rest.support;

import java.io.Serializable;

import org.hibernate.validator.constraints.NotEmpty;

public abstract class FileEditRequest implements Serializable {

	private static final long serialVersionUID = 1L;

	private String commitMessage;

	@NotEmpty
	public String getCommitMessage() {
		return commitMessage;
	}

	public void setCommitMessage(String commitMessage) {
		this.commitMessage = commitMessage;
	}

}