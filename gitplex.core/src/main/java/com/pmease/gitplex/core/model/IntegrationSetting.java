package com.pmease.gitplex.core.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.pmease.commons.editable.annotation.Editable;

@SuppressWarnings("serial")
@Editable
public class IntegrationSetting implements Serializable {

	private List<BranchStrategy> branchStrategies = new ArrayList<>();

	private IntegrationStrategy defaultStrategy = IntegrationStrategy.MERGE_ALWAYS;

	@Editable
	@Valid
	@NotNull
	public List<BranchStrategy> getBranchStrategies() {
		return branchStrategies;
	}

	public void setBranchStrategies(List<BranchStrategy> branchStrategies) {
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
