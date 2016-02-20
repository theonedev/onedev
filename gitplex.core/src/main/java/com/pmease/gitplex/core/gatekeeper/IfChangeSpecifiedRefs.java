package com.pmease.gitplex.core.gatekeeper;

import org.eclipse.jgit.lib.ObjectId;
import org.hibernate.validator.constraints.NotEmpty;

import com.google.common.collect.Lists;
import com.pmease.commons.git.GitUtils;
import com.pmease.commons.wicket.editable.annotation.Editable;
import com.pmease.gitplex.core.gatekeeper.checkresult.CheckResult;
import com.pmease.gitplex.core.model.Depot;
import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.core.model.User;
import com.pmease.gitplex.core.util.editable.RefMatch;
import com.pmease.gitplex.core.util.includeexclude.IncludeExcludeUtils;

@SuppressWarnings("serial")
@Editable(order=100, icon="fa-code-fork", description=
		"This gate keeper will be passed if specified refs is being changed.")
public class IfChangeSpecifiedRefs extends AbstractGateKeeper {

	private String refMatch;
	
	@Editable(name="Refs to Match", order=100, description="Specify refs to match")
	@RefMatch
	@NotEmpty
	public String getRefMatch() {
		return refMatch;
	}

	public void setRefMatch(String refMatch) {
		this.refMatch = refMatch;
	}

	private CheckResult doCheck(String refName) {
		if (IncludeExcludeUtils.matches(refMatch, refName)) {
			return passed(Lists.newArrayList("Target ref matches '" + refMatch + "'."));
		} else {
			return failed(Lists.newArrayList("Target ref does not match '" + refMatch + "'."));
		}
	}
	
	@Override
	public CheckResult doCheckRequest(PullRequest request) {
		return doCheck(request.getTargetRef());
	}

	@Override
	protected CheckResult doCheckFile(User user, Depot depot, String branch, String file) {
		return doCheck(GitUtils.branch2ref(branch));
	}

	@Override
	protected CheckResult doCheckPush(User user, Depot depot, String refName, ObjectId oldCommit, ObjectId newCommit) {
		return doCheck(refName);
	}

}
