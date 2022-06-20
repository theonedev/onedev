package io.onedev.server.web.component.diff.revision;

import java.io.Serializable;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.Multiline;
import io.onedev.server.web.editable.annotation.OmitName;

@Editable(name="Commit Batched Suggestions")
public class SuggestionBatchApplyBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private String commitMessage;

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
