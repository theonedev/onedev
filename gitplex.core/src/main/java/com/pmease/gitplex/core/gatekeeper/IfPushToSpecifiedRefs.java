package com.pmease.gitplex.core.gatekeeper;

import org.hibernate.validator.constraints.NotEmpty;

import com.google.common.collect.Lists;
import com.pmease.commons.editable.annotation.Editable;
import com.pmease.commons.util.pattern.WildcardUtils;
import com.pmease.gitplex.core.gatekeeper.checkresult.CheckResult;
import com.pmease.gitplex.core.model.Branch;
import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.core.model.User;

@Editable(order=200, icon="pa-branch-pattern", description="This gate keeper will be passed if push to specified references.")
@SuppressWarnings("serial")
public class IfPushToSpecifiedRefs extends AbstractGateKeeper {

	private String refPatterns;
	
	@Editable(name="Specify Ref Patterns", description="Specify ref patterns to match. Below is some examples:"
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
		return doCheckRef(request.getSubmitter(), request.getTarget().getRepository(), request.getTarget().getHeadRef());
	}

	@Override
	protected CheckResult doCheckFile(User user, Branch branch, String file) {
		return doCheckRef(user, branch.getRepository(), branch.getHeadRef());
	}

	@Override
	protected CheckResult doCheckCommit(User user, Branch branch, String commit) {
		return doCheckRef(user, branch.getRepository(), branch.getHeadRef());
	}

	@Override
	protected CheckResult doCheckRef(User user, Repository repository, String refName) {
		if (WildcardUtils.matchPath(getRefPatterns(), refName))
			return passed(Lists.newArrayList("Target ref matches pattern '" + refPatterns + "'."));
		else
			return failed(Lists.newArrayList("Target ref does not match pattern '" + refPatterns + "'."));
	}

}
