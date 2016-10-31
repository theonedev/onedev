package com.gitplex.core.event;

import org.eclipse.jgit.lib.ObjectId;

import com.gitplex.core.entity.Depot;

public class RefUpdated {
	
	private final Depot depot;
	
	private final String refName;
	
	private final ObjectId oldCommit;
	
	private final ObjectId newCommit;
	
	public RefUpdated(Depot depot, String refName, ObjectId oldCommit, ObjectId newCommit) {
		this.depot = depot;
		this.refName = refName;
		this.oldCommit = oldCommit;
		this.newCommit = newCommit;
	}

	public Depot getDepot() {
		return depot;
	}

	public String getRefName() {
		return refName;
	}

	public ObjectId getOldCommit() {
		return oldCommit;
	}

	public ObjectId getNewCommit() {
		return newCommit;
	}
	
}
