package com.pmease.gitop.core.gatekeeper;

import java.util.Collection;

import javax.validation.constraints.Min;

import com.google.common.base.Preconditions;
import com.pmease.commons.editable.annotation.Editable;
import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.core.manager.BuildResultManager;
import com.pmease.gitop.model.Branch;
import com.pmease.gitop.model.BuildResult;
import com.pmease.gitop.model.Project;
import com.pmease.gitop.model.PullRequest;
import com.pmease.gitop.model.User;
import com.pmease.gitop.model.gatekeeper.AbstractGateKeeper;
import com.pmease.gitop.model.gatekeeper.checkresult.CheckResult;
import com.pmease.gitop.model.gatekeeper.voteeligibility.NoneCanVote;

@Editable(icon="icon-checkbox-checked", order=1000, 
		description="This gate keeper will be satisfied if commit is verified successfully "
				+ "by specified number of builds. To make this working, your CI system has to "
				+ "be configured to build against Gitop pull requests.")
@SuppressWarnings("serial")
public class IfVerifiedByBuilds extends AbstractGateKeeper {
	
	private int buildCount = 1;
	
	private boolean checkMerged = true;
	
	private boolean blockMode = true;
	
	@Editable(order=100, description="This specified number of builds has to be reported successful "
			+ "for this gate keeper to be passed. Normally this number represents number of build "
			+ "configurations setting up to verify the branch.")
	@Min(1)
	public int getBuildCount() {
		return buildCount;
	}

	public void setBuildCount(int buildCount) {
		this.buildCount = buildCount;
	}

	@Editable(order=200, description="Enable this to check the merged commit instead of head commit.")
	public boolean isCheckMerged() {
		return checkMerged;
	}

	public void setCheckMerged(boolean checkMerged) {
		this.checkMerged = checkMerged;
	}

	@Editable(order=300, description="If this is checked, subsequent gate keepers will not be checked "
			+ "while waiting for the build results. This can be used to only notify relevant voters "
			+ "when the commit passes build.")
	public boolean isBlockMode() {
		return blockMode;
	}

	public void setBlockMode(boolean blockMode) {
		this.blockMode = blockMode;
	}

	@Override
	protected CheckResult doCheckRequest(PullRequest request) {
		BuildResultManager BuildResultManager = Gitop.getInstance(BuildResultManager.class);

		Preconditions.checkNotNull(request.getMergeResult());
		
		String commit;
		if (isCheckMerged()) {
			commit = request.getMergeResult().getMergeHead();
			if (commit == null) 
				return disapproved("Can not build against merged result due to conflicts.");
		} else if (request.isNew()) {
			commit = request.getSource().getHeadCommit();
		} else {
			commit = request.getLatestUpdate().getHeadCommit();
		}
		Collection<BuildResult> buildResults = BuildResultManager.findBy(commit);
		for (BuildResult each: buildResults) {
			if (!each.isPassed())
				return disapproved("At least one build is failed for the merged commit.");
		}
		int lacks = buildCount - buildResults.size();
		
		if (lacks > 0) {
			if (blockMode) {
				if (buildCount > 1)
					return pendingAndBlock("To be verified by " + lacks + " more build(s)", new NoneCanVote());
				else
					return pendingAndBlock("To be verified by build", new NoneCanVote());
			} else {
				if (buildCount > 1)
					return pending("To be verified by " + lacks + " more build(s)", new NoneCanVote());
				else
					return pending("To be verified by build", new NoneCanVote());
			}
		} else {
			return approved("Builds passed");
		}
	}

	@Override
	protected CheckResult doCheckFile(User user, Branch branch, String file) {
		if (blockMode)
			return pendingAndBlock("Not verified by build.", new NoneCanVote());
		else
			return pending("Not verified by build.", new NoneCanVote());
	}

	@Override
	protected CheckResult doCheckCommit(User user, Branch branch, String commit) {
		BuildResultManager BuildResultManager = Gitop.getInstance(BuildResultManager.class);

		Collection<BuildResult> buildResults = BuildResultManager.findBy(commit);
		for (BuildResult each: buildResults) {
			if (!each.isPassed())
				return disapproved("At least one build is failed for the commit.");
		}
		int lacks = buildCount - buildResults.size();
		
		if (lacks > 0) {
			if (blockMode) {
				if (buildCount > 1)
					return pendingAndBlock("Lack verifications of " + lacks + " more build(s)", new NoneCanVote());
				else
					return pendingAndBlock("Not verified by build", new NoneCanVote());
			} else {
				if (buildCount > 1)
					return pending("Lack verifications of " + lacks + " more build(s)", new NoneCanVote());
				else
					return pending("Not verified by build", new NoneCanVote());
			}
		} else {
			return approved("Builds passed");
		}
	}

	@Override
	protected CheckResult doCheckRef(User user, Project project, String refName) {
		return ignored();
	}

}
