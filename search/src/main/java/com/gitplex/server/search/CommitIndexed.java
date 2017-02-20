package com.gitplex.server.search;

import org.eclipse.jgit.lib.ObjectId;

import com.gitplex.server.model.Depot;

public class CommitIndexed {
	
	private final Depot depot;
	
	private final ObjectId commitId;
	
	public CommitIndexed(Depot depot, ObjectId commitId) {
		this.depot = depot;
		this.commitId = commitId;
	}

	public Depot getDepot() {
		return depot;
	}

	public ObjectId getCommitId() {
		return commitId;
	}
	
}
