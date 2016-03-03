package com.pmease.gitplex.core.gatekeeper;

import java.util.Collection;

import javax.validation.constraints.Min;

import org.eclipse.jgit.lib.ObjectId;

import com.google.common.collect.Lists;
import com.pmease.commons.wicket.editable.annotation.Editable;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.entity.Depot;
import com.pmease.gitplex.core.entity.PullRequest;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.entity.Verification;
import com.pmease.gitplex.core.entity.component.IntegrationPreview;
import com.pmease.gitplex.core.gatekeeper.checkresult.CheckResult;
import com.pmease.gitplex.core.manager.VerificationManager;

@Editable(icon="fa-check-circle", order=1000,
		description="This gate keeper will be satisfied if commit is verified successfully "
				+ "by specified number of builds. To make this working, your CI system has to "
				+ "be configured to build against GitPlex pull requests")
public class IfVerifiedByBuilds extends AbstractGateKeeper {
	
	private static final long serialVersionUID = 1L;
	
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
				return blocking(Lists.newArrayList("To be verified by build"));
			else
				return pending(Lists.newArrayList("To be verified by build"));
		}
		
		String commit;
		if (isCheckIntegrated()) {
			IntegrationPreview preview = request.getIntegrationPreview();
			if (preview == null) {
				if (blockMode)
					return blocking(Lists.newArrayList("To be verified by build"));
				else
					return pending(Lists.newArrayList("To be verified by build"));
			}
			commit = preview.getIntegrated();
			if (commit == null) 
				return failed(Lists.newArrayList("Can not build against integration result due to conflicts"));
		} else {
			commit = request.getLatestUpdate().getHeadCommitHash();
		}

		VerificationManager verificationManager = GitPlex.getInstance(VerificationManager.class);

		int passedCount = 0;
		Collection<Verification> verifications = verificationManager.findBy(request, commit);
		for (Verification each: verifications) {
			if (each.getStatus() == Verification.Status.NOT_PASSED)
				return failed(Lists.newArrayList("At least one build is not passed for the commit"));
			else if (each.getStatus() == Verification.Status.PASSED)
				passedCount++;
		}
		int lacks = leastPassCount - passedCount;
		
		if (lacks > 0) {
			if (blockMode) {
				if (leastPassCount > 1)
					return blocking(Lists.newArrayList("To be verified by " + lacks + " more build(s)"));
				else
					return blocking(Lists.newArrayList("To be verified by build"));
			} else {
				if (leastPassCount > 1)
					return pending(Lists.newArrayList("To be verified by " + lacks + " more build(s)"));
				else
					return pending(Lists.newArrayList("To be verified by build"));
			}
		} else {
			return passed(Lists.newArrayList("Builds passed"));
		}
	}

	@Override
	protected CheckResult doCheckFile(Account user, Depot depot, String branch, String file) {
		if (blockMode)
			return blocking(Lists.newArrayList("Not verified by build"));
		else
			return pending(Lists.newArrayList("Not verified by build"));
	}

	@Override
	protected CheckResult doCheckPush(Account user, Depot depot, String refName, ObjectId oldCommit, ObjectId newCommit) {
		if (!newCommit.equals(ObjectId.zeroId())) {
			if (blockMode) {
				return blocking(Lists.newArrayList("Has to be verified by builds"));
			} else {
				return blocking(Lists.newArrayList("Has to be verified by builds"));
			}
		} else {
			return ignored();
		}
	}

}
