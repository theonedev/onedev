package com.pmease.gitop.core.gatekeeper;

import org.hibernate.validator.constraints.NotEmpty;

import com.pmease.commons.editable.annotation.Editable;
import com.pmease.commons.util.pattern.WildcardUtils;
import com.pmease.gitop.model.Branch;
import com.pmease.gitop.model.Project;
import com.pmease.gitop.model.PullRequest;
import com.pmease.gitop.model.User;
import com.pmease.gitop.model.gatekeeper.BranchGateKeeper;
import com.pmease.gitop.model.gatekeeper.checkresult.CheckResult;

@SuppressWarnings("serial")
@Editable(order=200, icon="icon-git-branch-pattern", description=
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

	@Override
	public CheckResult doCheckRequest(PullRequest request) {
		if (WildcardUtils.matchPath(getBranchPatterns(), request.getTarget().getName()))
			return accepted("Target branch matches pattern '" + branchPatterns + "'.");
		else
			return rejected("Target branch does not match pattern '" + branchPatterns + "'.");
	}

	@Override
	protected CheckResult doCheckFile(User user, Branch branch, String file) {
		if (WildcardUtils.matchPath(getBranchPatterns(), branch.getName()))
			return accepted("Target branch matches pattern '" + branchPatterns + "'.");
		else
			return rejected("Target branch does not match pattern '" + branchPatterns + "'.");
	}

	@Override
	protected CheckResult doCheckCommit(User user, Branch branch, String commit) {
		if (WildcardUtils.matchPath(getBranchPatterns(), branch.getName()))
			return accepted("Target branch matches pattern '" + branchPatterns + "'.");
		else
			return rejected("Target branch does not match pattern '" + branchPatterns + "'.");
	}

	@Override
	protected CheckResult doCheckRef(User user, Project project, String refName) {
		return ignored();
	}

}
