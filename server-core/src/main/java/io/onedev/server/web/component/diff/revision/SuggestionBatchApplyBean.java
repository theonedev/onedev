package io.onedev.server.web.component.diff.revision;

import java.io.Serializable;

import javax.validation.constraints.NotEmpty;

import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.Multiline;
import io.onedev.server.annotation.OmitName;

@Editable(name="Commit Batched Suggestions")
public class SuggestionBatchApplyBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private String commitMessage = "Apply suggested changes from code comments";

	@Editable
	@OmitName
	@Multiline
	@NotEmpty
	public String getCommitMessage() {
		return commitMessage;
	}

	public void setCommitMessage(String commitMessage) {
		this.commitMessage = commitMessage;
	}
	
}
