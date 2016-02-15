package com.pmease.gitplex.core.gatekeeper;

import org.hibernate.validator.constraints.NotEmpty;

import com.google.common.collect.Lists;
import com.pmease.commons.git.GitUtils;
import com.pmease.commons.util.pattern.WildcardUtils;
import com.pmease.commons.wicket.editable.annotation.Editable;
import com.pmease.gitplex.core.gatekeeper.checkresult.CheckResult;
import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.core.model.Depot;
import com.pmease.gitplex.core.model.User;

@SuppressWarnings("serial")
@Editable(order=200, icon="fa-ext fa-branch-pattern", category=GateKeeper.CATEGROY_CHECK_BRANCH, description=
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

	private CheckResult checkBranch(Depot depot, String branch) {
		if (WildcardUtils.matchPath(getBranchPatterns(), branch))
			return passed(Lists.newArrayList("Target branch matches pattern '" + branchPatterns + "'."));
		else
			return failed(Lists.newArrayList("Target branch does not match pattern '" + branchPatterns + "'."));
	}
	
	@Override
	public CheckResult doCheckRequest(PullRequest request) {
		return checkBranch(request.getTargetDepot(), request.getTargetBranch());
	}

	@Override
	protected CheckResult doCheckFile(User user, Depot depot, String branch, String file) {
		return checkBranch(depot, branch);
	}

	@Override
	protected CheckResult doCheckCommit(User user, Depot depot, String branch, String commit) {
		return checkBranch(depot, branch);
	}

	@Override
	protected CheckResult doCheckRef(User user, Depot depot, String refName) {
		String branch = GitUtils.ref2branch(refName);
		if (branch != null)
			return checkBranch(depot, branch);
		else 
			return ignored();
	}

}
