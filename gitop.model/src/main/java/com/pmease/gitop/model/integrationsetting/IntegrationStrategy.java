package com.pmease.gitop.model.integrationsetting;

import java.io.Serializable;

import javax.validation.constraints.NotNull;

import com.pmease.commons.editable.annotation.Editable;
import com.pmease.commons.editable.annotation.Horizontal;
import com.pmease.gitop.model.helper.BranchMatcher;

@SuppressWarnings("serial")
@Editable
public class IntegrationStrategy implements Serializable {
	
	private BranchMatcher targetBranches;
	
	private boolean rebaseIfPossible;
	
	private boolean mergeAlways;

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
	public boolean isRebaseIfPossible() {
		return rebaseIfPossible;
	}

	public void setRebaseIfPossible(boolean rebaseIfPossible) {
		this.rebaseIfPossible = rebaseIfPossible;
	}

	@Editable(order=300)
	public boolean isMergeAlways() {
		return mergeAlways;
	}

	public void setMergeAlways(boolean mergeAlways) {
		this.mergeAlways = mergeAlways;
	}
	
}
