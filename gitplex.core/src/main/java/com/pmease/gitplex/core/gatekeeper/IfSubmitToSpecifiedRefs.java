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
import com.pmease.gitplex.core.util.refmatch.RefMatchUtils;

@SuppressWarnings("serial")
@Editable(order=100, icon="fa-ext fa-branch", category=GateKeeper.CATEGROY_CHECK_REFS, description=
		"This gate keeper will be passed if the commit is submitted to specified refs.")
public class IfSubmitToSpecifiedRefs extends AbstractGateKeeper {

	private String refMatch;
	
	@Editable(name="Refs", description="Specify refs to match")
	@RefMatch
	@NotEmpty
	public String getRefMatch() {
		return refMatch;
	}

	public void setRefMatch(String refMatch) {
		this.refMatch = refMatch;
	}

	@Override
	public CheckResult doCheckRequest(PullRequest request) {
		return checkRef(request.getTargetDepot(), request.getTargetRef());
	}

	private CheckResult checkRef(Depot depot, String refName) {
		if (RefMatchUtils.matches(refMatch, refName))
			return passed(Lists.newArrayList("Target ref matches '" + refMatch + "'."));
		else
			return failed(Lists.newArrayList("Target ref does not match '" + refMatch + "'."));
	}
	
	@Override
	protected CheckResult doCheckFile(User user, Depot depot, String branch, String file) {
		return checkRef(depot, GitUtils.branch2ref(branch));
	}

	@Override
	protected CheckResult doCheckPush(User user, Depot depot, String refName, ObjectId oldCommit, ObjectId newCommit) {
		return checkRef(depot, refName);
	}

}
