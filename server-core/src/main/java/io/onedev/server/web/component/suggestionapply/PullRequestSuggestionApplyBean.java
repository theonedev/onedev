package io.onedev.server.web.component.suggestionapply;

import javax.validation.constraints.NotEmpty;

import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.Multiline;
import io.onedev.server.annotation.OmitName;

@Editable(name="Commit Suggestion")
public class PullRequestSuggestionApplyBean extends SuggestionApplyBean {
	
	@Editable
	@Override
	public String getBranch() {
		return super.getBranch();
	}

	@Editable(order=200)
	@OmitName
	@Multiline
	@NotEmpty
	public String getCommitMessage() {
		return super.getCommitMessage();
	}

	@Override
	public void setCommitMessage(String commitMessage) {
		super.setCommitMessage(commitMessage);
	}
	
}
