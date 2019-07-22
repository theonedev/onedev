package io.onedev.server.model.support.issue.transitiontrigger;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.server.web.editable.annotation.BranchChoice;
import io.onedev.server.web.editable.annotation.Editable;

public abstract class PullRequestTrigger implements TransitionTrigger {

	private static final long serialVersionUID = 1L;
	
	private String branch;
	
	@Editable(name="Target Branch")
	@BranchChoice
	@NotEmpty
	public String getBranch() {
		return branch;
	}

	public void setBranch(String branch) {
		this.branch = branch;
	}

}
