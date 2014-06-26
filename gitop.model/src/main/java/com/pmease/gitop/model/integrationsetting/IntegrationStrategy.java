package com.pmease.gitop.model.integrationsetting;

import java.io.Serializable;

import com.pmease.commons.editable.annotation.Editable;

@SuppressWarnings("serial")
@Editable
public class IntegrationStrategy implements Serializable {
	
	private boolean tryRebaseFirst;
	
	private boolean mergeAlwaysOtherwise;

	@Editable(order=100)
	public boolean isTryRebaseFirst() {
		return tryRebaseFirst;
	}

	public void setTryRebaseFirst(boolean tryRebaseFirst) {
		this.tryRebaseFirst = tryRebaseFirst;
	}

	@Editable(order=200)
	public boolean isMergeAlwaysOtherwise() {
		return mergeAlwaysOtherwise;
	}

	public void setMergeAlwaysOtherwise(boolean mergeAlwaysOtherwise) {
		this.mergeAlwaysOtherwise = mergeAlwaysOtherwise;
	}

}
