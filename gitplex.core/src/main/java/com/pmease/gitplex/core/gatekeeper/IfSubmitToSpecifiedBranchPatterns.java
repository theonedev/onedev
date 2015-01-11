package com.pmease.gitplex.core.gatekeeper;

import org.hibernate.validator.constraints.NotEmpty;

import com.google.common.collect.Lists;
import com.pmease.commons.editable.annotation.Editable;
import com.pmease.commons.util.pattern.WildcardUtils;
import com.pmease.gitplex.core.gatekeeper.checkresult.CheckResult;
import com.pmease.gitplex.core.model.Branch;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.core.model.User;

@SuppressWarnings("serial")
@Editable(order=200, icon="pa-branch-pattern", category=GateKeeper.CATEGROY_CHECK_BRANCH, description=
		"This gate keeper will be passed if the commit is submitted to specified branch pattern.")
public class IfSubmitToSpecifiedBranchPatterns extends AbstractGateKeeper {

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

	private CheckResult checkBranch(String branchName) {
		if (WildcardUtils.matchPath(getBranchPatterns(), branchName))
			return passed(Lists.newArrayList("Target branch matches pattern '" + branchPatterns + "'."));
		else
			return failed(Lists.newArrayList("Target branch does not match pattern '" + branchPatterns + "'."));
	}
	
	@Override
	public CheckResult doCheckRequest(PullRequest request) {
		return checkBranch(request.getTarget().getName());
	}

	@Override
	protected CheckResult doCheckFile(User user, Branch branch, String file) {
		return checkBranch(branch.getName());
	}

	@Override
	protected CheckResult doCheckCommit(User user, Branch branch, String commit) {
		return checkBranch(branch.getName());
	}

	@Override
	protected CheckResult doCheckRef(User user, Repository repository, String refName) {
		String branchName = Branch.parseName(refName);
		if (branchName != null)
			return checkBranch(branchName);
		else 
			return ignored();
	}

}
