package io.onedev.server.issue.transitiontrigger;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.server.util.Usage;
import io.onedev.server.web.editable.annotation.BranchChoice;
import io.onedev.server.web.editable.annotation.Editable;

public abstract class PullRequestTrigger extends TransitionTrigger {

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

	@Override
	public Usage onDeleteBranch(String branchName) {
		Usage usage = new Usage();
		if (getBranch().equals(branchName))
			usage.add("pull request trigger: target branch");
		return usage;
	}
	
}
