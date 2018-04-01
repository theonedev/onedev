package io.onedev.server.model.support.issueworkflow.action;

import io.onedev.server.util.editable.annotation.Editable;

@Editable(order=300, name="Pull request is discarded for")
public class DiscardPullRequest implements IssueAction {

	private static final long serialVersionUID = 1L;

	private String targetBranch;

	@Editable
	public String getTargetBranch() {
		return targetBranch;
	}

	public void setTargetBranch(String targetBranch) {
		this.targetBranch = targetBranch;
	}

	@Override
	public Button getButton() {
		return null;
	}
	
}
