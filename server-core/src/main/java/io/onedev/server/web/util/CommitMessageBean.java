package io.onedev.server.web.util;

import java.io.Serializable;

import javax.validation.constraints.NotEmpty;

import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.Multiline;
import io.onedev.server.annotation.OmitName;
import io.onedev.server.annotation.ReferenceAware;

@Editable
public class CommitMessageBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private String commitMessage;
	
	@Editable(order=100, name="Commit Message")
	@Multiline
	@OmitName
	@ReferenceAware
	@NotEmpty
	public String getCommitMessage() {
		return commitMessage;
	}

	public void setCommitMessage(String commitMessage) {
		this.commitMessage = commitMessage;
	}
	
}
