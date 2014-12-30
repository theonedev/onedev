package com.pmease.gitplex.core.gatekeeper;

import org.hibernate.validator.constraints.NotEmpty;

import com.pmease.commons.editable.annotation.Editable;
import com.pmease.commons.util.pattern.WildcardUtils;
import com.pmease.gitplex.core.gatekeeper.checkresult.CheckResult;
import com.pmease.gitplex.core.model.Branch;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.core.model.User;

@SuppressWarnings("serial")
@Editable(order=200, icon="pa-branch-pattern", description=
		"This gate keeper will be passed if the commit is submitted to specified branch pattern.")
public class IfSubmitToSpecifiedBranchPatterns extends BranchGateKeeper {

	private String branchPatterns;
	
	@Editable(name="Specify Branch Patterns", description="Specify branch patterns to match. Below is some examples:"
			+ "<ul>"
			+ "<li><i>dev/*</i>: matches all branches directly under dev."
			+ "<li><i>dev/**</i>: matches all branches under dev recursively."
			+ "<li><i>**</i>: matches all branches."
			+ "<li><i>**/bugfix</i>: matches all branches whose last segment is bugfix."
			+ "<li><i>-dev/**, **</i>: matches all branches except those under dev."
			+ "</ul>")
	@NotEmpty
	public String getBranchPatterns() {
		return branchPatterns;
	}

	public void setBranchPatterns(String branchPatterns) {
		this.branchPatterns = branchPatterns;
	}

	private CheckResult checkBranch(Branch branch) {
		if (WildcardUtils.matchPath(getBranchPatterns(), branch.getName()))
			return passed("Target branch matches pattern '" + branchPatterns + "'.");
		else
			return failed("Target branch does not match pattern '" + branchPatterns + "'.");
	}
	
	@Override
	public CheckResult doCheckRequest(PullRequest request) {
		return checkBranch(request.getTarget());
	}

	@Override
	protected CheckResult doCheckFile(User user, Branch branch, String file) {
		return checkBranch(branch);
	}

	@Override
	protected CheckResult doCheckCommit(User user, Branch branch, String commit) {
		return checkBranch(branch);
	}

	@Override
	protected CheckResult doCheckRef(User user, Repository repository, String refName) {
		return ignored();
	}

}
