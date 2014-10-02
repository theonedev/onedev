package com.pmease.gitplex.core.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.pmease.commons.editable.annotation.Editable;
import com.pmease.gitplex.core.model.PullRequest.IntegrationStrategy;

@SuppressWarnings("serial")
@Editable
public class IntegrationSetting implements Serializable {

	private List<IntegrationPolicy> branchStrategies = new ArrayList<>();

	private IntegrationStrategy defaultStrategy = IntegrationStrategy.MERGE_ALWAYS;

	@Editable
	@Valid
	@NotNull
	public List<IntegrationPolicy> getBranchStrategies() {
		return branchStrategies;
	}

	public void setBranchStrategies(List<IntegrationPolicy> branchStrategies) {
		this.branchStrategies = branchStrategies;
	}

	@Editable
	@Valid
	@NotNull
	public IntegrationStrategy getDefaultStrategy() {
		return defaultStrategy;
	}

	public void setDefaultStrategy(IntegrationStrategy defaultStrategy) {
		this.defaultStrategy = defaultStrategy;
	}

}
