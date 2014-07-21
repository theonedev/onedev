package com.pmease.gitplex.core.pullrequest;

import java.io.Serializable;

import javax.validation.constraints.NotNull;

import com.pmease.commons.editable.annotation.Editable;
import com.pmease.gitplex.core.branchmatcher.AffinalBranchMatcher;
import com.pmease.gitplex.core.branchmatcher.BranchMatcher;

@SuppressWarnings("serial")
@Editable
public class BranchStrategy implements Serializable {
	
	private BranchMatcher targetBranches;

	private AffinalBranchMatcher sourceBranches;
	
	private IntegrationStrategy integrationStrategy = IntegrationStrategy.MERGE_ALWAYS;
	
	@Editable(name="Pull Request Target", order=100)
	@NotNull
	public BranchMatcher getTargetBranches() {
		return targetBranches;
	}

	public void setTargetBranches(BranchMatcher targetBranches) {
		this.targetBranches = targetBranches;
	}

	@Editable(name="Pull Request Source", order=200)
	@NotNull
	public AffinalBranchMatcher getSourceBranches() {
		return sourceBranches;
	}

	public void setSourceBranches(AffinalBranchMatcher sourceBranches) {
		this.sourceBranches = sourceBranches;
	}

	@Editable(order=300)
	public IntegrationStrategy getIntegrationStrategy() {
		return integrationStrategy;
	}

	public void setIntegrationStrategy(IntegrationStrategy integrationStrategy) {
		this.integrationStrategy = integrationStrategy;
	}

}
