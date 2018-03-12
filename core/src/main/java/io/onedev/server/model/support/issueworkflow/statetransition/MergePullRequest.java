package io.onedev.server.model.support.issueworkflow.statetransition;

import io.onedev.server.model.support.issueworkflow.IssueAction;
import io.onedev.server.model.support.issueworkflow.StateTransition;
import io.onedev.server.util.editable.annotation.Editable;

@Editable(order=300)
public class MergePullRequest extends StateTransition {

	private static final long serialVersionUID = 1L;

	private String targetBranch;

	@Override
	public IssueAction getAction() {
		return null;
	}

	@Editable
	public String getTargetBranch() {
		return targetBranch;
	}

	public void setTargetBranch(String targetBranch) {
		this.targetBranch = targetBranch;
	}
	
}
