package com.pmease.gitplex.core.gatekeeper;

import org.hibernate.validator.constraints.NotEmpty;

import com.pmease.commons.editable.annotation.Editable;
import com.pmease.commons.git.Git;
import com.pmease.commons.util.pattern.WildcardUtils;
import com.pmease.gitplex.core.gatekeeper.checkresult.CheckResult;
import com.pmease.gitplex.core.model.Branch;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.core.model.User;

@Editable(order=200, icon="pa-branch-pattern", name="If Push To Specified Non-Branch Refs", description=
		"This gate keeper will be passed if push to specified non-branch references. Note that "
		+ "touching various branch references under refs/heads will not trigger this gate "
		+ "keeper. Use branch related gate keepers if you would like to match desired branches.")
@SuppressWarnings("serial")
public class IfPushToSpecifiedNonBranchRefs extends AbstractGateKeeper {

	private String refPatterns;
	
	@Editable(name="Specify Non-Branch Ref Patterns", description="Specify non-branch ref patterns to match. Below is some examples:"
			+ "<ul>"
			+ "<li><i>refs/tags/*</i>: matches all refs directly under refs/tags."
			+ "<li><i>refs/tags/**</i>: matches all refs under refs/tags recursively."
			+ "<li><i>refs/**</i>: matches all refs."
			+ "<li><i>refs/**/1.0</i>: matches all refs whose last segment is 1.0."
			+ "<li><i>-refs/tags/release/**, refs/tags/**</i>: matches all refs under refs/tags except those under refs/tags/release."
			+ "</ul>")
	@NotEmpty
	public String getRefPatterns() {
		return refPatterns;
	}

	public void setRefPatterns(String refPatterns) {
		this.refPatterns = refPatterns;
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
			return ignored();
		} else {
			if (WildcardUtils.matchPath(getRefPatterns(), refName))
				return passed("Target ref matches pattern '" + refPatterns + "'.");
			else
				return failed("Target ref does not match pattern '" + refPatterns + "'.");
		}
	}

}
