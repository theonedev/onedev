package com.pmease.gitop.core.gatekeeper;

import org.hibernate.validator.constraints.NotEmpty;

import com.pmease.commons.editable.annotation.Editable;
import com.pmease.commons.git.Git;
import com.pmease.commons.util.pattern.WildcardUtils;
import com.pmease.gitop.model.Branch;
import com.pmease.gitop.model.Repository;
import com.pmease.gitop.model.PullRequest;
import com.pmease.gitop.model.User;
import com.pmease.gitop.model.gatekeeper.BranchGateKeeper;
import com.pmease.gitop.model.gatekeeper.checkresult.CheckResult;

@Editable(order=210, icon="icon-git-branch-pattern", description=
		"This gate keeper will be passed if the branch to be created matches specified patterns.")
@SuppressWarnings("serial")
public class IfCreateSpecifiedBranches extends BranchGateKeeper {

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
	protected CheckResult doCheckRequest(PullRequest request) {
		return ignored();
	}

	@Override
	protected CheckResult doCheckFile(User user, Branch branch, String file) {
		return ignored();
	}

	@Override
	protected CheckResult doCheckCommit(User user, Branch branch, String commit) {
		return ignored();
	}

	@Override
	protected CheckResult doCheckRef(User user, Repository repository, String refName) {
		if (refName.startsWith(Git.REFS_HEADS)) {
			if (WildcardUtils.matchPath(Git.REFS_HEADS + getBranchPatterns(), refName))
				return approved("Branch to be created matches pattern '" + branchPatterns + "'.");
			else
				return disapproved("Branch to be created does not match pattern '" + branchPatterns + "'.");
		} else {
			return ignored();
		}
	}

}
