package com.pmease.gitop.model.integrationsetting;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;

import com.pmease.commons.editable.annotation.Editable;
import com.pmease.gitop.model.helper.BranchMatcher;

@SuppressWarnings("serial")
@Editable
public class IntegrationSetting implements Serializable {

	private BranchMatcher rebasibleBranches;
	
	private List<IntegrationStrategy> downstreamStrategies = new ArrayList<>();
	
	private List<IntegrationStrategy> upstreamStrategies = new ArrayList<>();

	@Editable(order=100)
	public BranchMatcher getRebasibleBranches() {
		return rebasibleBranches;
	}

	public void setRebasibleBranches(BranchMatcher rebasibleBranches) {
		this.rebasibleBranches = rebasibleBranches;
	}

	@Editable(order=200)
	@NotNull
	public List<IntegrationStrategy> getDownstreamStrategies() {
		return downstreamStrategies;
	}

	public void setDownstreamStrategies(
			List<IntegrationStrategy> downstreamStrategies) {
		this.downstreamStrategies = downstreamStrategies;
	}

	@Editable(order=300)
	@NotNull
	public List<IntegrationStrategy> getUpstreamStrategies() {
		return upstreamStrategies;
	}

	public void setUpstreamStrategies(List<IntegrationStrategy> upstreamStrategies) {
		this.upstreamStrategies = upstreamStrategies;
	}

}
