package com.pmease.gitplex.core.gatekeeper;

import java.util.Collection;

import javax.validation.constraints.Min;

import com.pmease.commons.editable.annotation.Editable;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.gatekeeper.checkresult.CheckResult;
import com.pmease.gitplex.core.gatekeeper.voteeligibility.NoneCanVote;
import com.pmease.gitplex.core.manager.PullRequestManager;
import com.pmease.gitplex.core.manager.VerificationManager;
import com.pmease.gitplex.core.model.Branch;
import com.pmease.gitplex.core.model.IntegrationPreview;
import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.core.model.User;
import com.pmease.gitplex.core.model.PullRequestVerification;

@Editable(icon="fa-checkbox-checked", order=1000, 
		description="This gate keeper will be satisfied if commit is verified successfully "
				+ "by specified number of builds. To make this working, your CI system has to "
				+ "be configured to build against GitPlex pull requests.")
@SuppressWarnings("serial")
public class IfVerifiedByBuilds extends AbstractGateKeeper {
	
	private int leastPassCount = 1;
	
	private boolean checkIntegrated = true;
	
	private boolean blockMode = true;
	
	@Editable(order=100, description="This specified number of builds has to be reported successful "
			+ "for this gate keeper to be passed. Normally this number represents number of build "
			+ "configurations setting up to verify the branch.")
	@Min(1)
	public int getLeastPassCount() {
		return leastPassCount;
	}

	public void setLeastPassCount(int leastPassCount) {
		this.leastPassCount = leastPassCount;
	}

	@Editable(order=200, description="Enable this to check the integrated commit instead of head commit.")
	public boolean isCheckIntegrated() {
		return checkIntegrated;
	}

	public void setCheckIntegrated(boolean checkIntegrated) {
		this.checkIntegrated = checkIntegrated;
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
		if (request.isNew()) {
			if (blockMode)
				return pendingAndBlock("To be verified by build", new NoneCanVote());
			else
				return pending("To be verified by build", new NoneCanVote());
		}
		
		String commit;
		if (isCheckIntegrated()) {
			PullRequestManager pullRequestManager = GitPlex.getInstance(PullRequestManager.class);
			
			IntegrationPreview preview = pullRequestManager.previewIntegration(request);
			if (preview == null) {
				if (blockMode)
					return pendingAndBlock("To be verified by build", new NoneCanVote());
				else
					return pending("To be verified by build", new NoneCanVote());
			}
			commit = preview.getIntegrated();
			if (commit == null) 
				return disapproved("Can not build against integration result due to conflicts.");
		} else {
			commit = request.getLatestUpdate().getHeadCommitHash();
		}

		VerificationManager verificationManager = GitPlex.getInstance(VerificationManager.class);

		int passedCount = 0;
		Collection<PullRequestVerification> verifications = verificationManager.findBy(request, commit);
		for (PullRequestVerification each: verifications) {
			if (each.getStatus() == PullRequestVerification.Status.NOT_PASSED)
				return disapproved("At least one build is not passed for the commit.");
			else if (each.getStatus() == PullRequestVerification.Status.PASSED)
				passedCount++;
		}
		int lacks = leastPassCount - passedCount;
		
		if (lacks > 0) {
			if (blockMode) {
				if (leastPassCount > 1)
					return pendingAndBlock("To be verified by " + lacks + " more build(s)", new NoneCanVote());
				else
					return pendingAndBlock("To be verified by build", new NoneCanVote());
			} else {
				if (leastPassCount > 1)
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
		if (blockMode) {
			return pendingAndBlock("Has to be verified by builds.", new NoneCanVote());
		} else {
			return pendingAndBlock("Has to be verified by builds.", new NoneCanVote());
		}
	}

	@Override
	protected CheckResult doCheckRef(User user, Repository repository, String refName) {
		return ignored();
	}

}
