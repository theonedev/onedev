package com.pmease.gitop.model.integrationsetting;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.pmease.commons.editable.annotation.Editable;
import com.pmease.commons.editable.annotation.Horizontal;
import com.pmease.gitop.model.helper.BranchMatcher;

@SuppressWarnings("serial")
@Editable
public class IntegrationSetting implements Serializable {

	private BranchMatcher rebasibleBranches;
	
	private List<BranchStrategy> downstreamStrategies = new ArrayList<>();
	
	private IntegrationStrategy defaultDownstreamStrategy = new IntegrationStrategy();
	
	private List<BranchStrategy> upstreamStrategies = new ArrayList<>();
	
	private IntegrationStrategy defaultUpstreamStrategy = new IntegrationStrategy();

	public IntegrationSetting() {
		defaultDownstreamStrategy.setTryRebaseFirst(false);
		defaultDownstreamStrategy.setMergeAlwaysOtherwise(true);
		
		defaultUpstreamStrategy.setTryRebaseFirst(false);
		defaultUpstreamStrategy.setMergeAlwaysOtherwise(false);
	}
	
	@Editable
	@Valid
	@Horizontal
	public BranchMatcher getRebasibleBranches() {
		return rebasibleBranches;
	}

	public void setRebasibleBranches(BranchMatcher rebasibleBranches) {
		this.rebasibleBranches = rebasibleBranches;
	}

	@Editable
	@Valid
	@NotNull
	public List<BranchStrategy> getDownstreamStrategies() {
		return downstreamStrategies;
	}

	public void setDownstreamStrategies(List<BranchStrategy> downstreamStrategies) {
		this.downstreamStrategies = downstreamStrategies;
	}

	@Editable
	@Valid
	@NotNull
	public IntegrationStrategy getDefaultDownstreamStrategy() {
		return defaultDownstreamStrategy;
	}

	public void setDefaultDownstreamStrategy(IntegrationStrategy defaultDownstreamStrategy) {
		this.defaultDownstreamStrategy = defaultDownstreamStrategy;
	}

	@Editable
	@Valid
	@NotNull
	public List<BranchStrategy> getUpstreamStrategies() {
		return upstreamStrategies;
	}

	public void setUpstreamStrategies(List<BranchStrategy> upstreamStrategies) {
		this.upstreamStrategies = upstreamStrategies;
	}

	@Editable
	@Valid
	@NotNull
	public IntegrationStrategy getDefaultUpstreamStrategy() {
		return defaultUpstreamStrategy;
	}

	public void setDefaultUpstreamStrategy(IntegrationStrategy defaultUpstreamStrategy) {
		this.defaultUpstreamStrategy = defaultUpstreamStrategy;
	}

}
