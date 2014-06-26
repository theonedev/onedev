package com.pmease.gitop.model.integrationsetting;

import java.io.Serializable;

import javax.validation.constraints.NotNull;

import com.pmease.commons.editable.annotation.Editable;
import com.pmease.commons.editable.annotation.Horizontal;
import com.pmease.gitop.model.helper.BranchMatcher;

@SuppressWarnings("serial")
@Editable
public class BranchStrategy implements Serializable {
	
	private BranchMatcher targetBranches;
	
	private IntegrationStrategy integrationStrategy = new IntegrationStrategy();
	
	@Editable(order=100)
	@NotNull
	@Horizontal
	public BranchMatcher getTargetBranches() {
		return targetBranches;
	}

	public void setTargetBranches(BranchMatcher targetBranches) {
		this.targetBranches = targetBranches;
	}

	@Editable(order=200)
	public boolean isTryRebaseFirst() {
		return integrationStrategy.isTryRebaseFirst();
	}

	public void setTryRebaseFirst(boolean tryRebaseFirst) {
		integrationStrategy.setTryRebaseFirst(tryRebaseFirst);
	}

	@Editable(order=300)
	public boolean isMergeAlwaysOtherwise() {
		return integrationStrategy.isMergeAlwaysOtherwise();
	}

	public void setMergeAlwaysOtherwise(boolean mergeAlwaysOtherwise) {
		integrationStrategy.setMergeAlwaysOtherwise(mergeAlwaysOtherwise);
	}
	
}
