package com.pmease.gitop.core.event;

import com.pmease.gitop.model.Branch;

public class BranchRefUpdateEvent {
	
	private final Branch branch;
	
	public BranchRefUpdateEvent(Branch branch) {
		this.branch = branch;
	}

	public Branch getBranch() {
		return branch;
	}
	
}
