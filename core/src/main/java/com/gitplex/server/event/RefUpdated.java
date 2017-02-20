package com.gitplex.server.event;

import org.eclipse.jgit.lib.ObjectId;

import com.gitplex.server.model.Depot;

public class RefUpdated {
	
	private final Depot depot;
	
	private final String refName;
	
	private final ObjectId oldObjectId;
	
	private final ObjectId newObjectId;
	
	public RefUpdated(Depot depot, String refName, ObjectId oldObjectId, ObjectId newObjectId) {
		this.depot = depot;
		this.refName = refName;
		this.oldObjectId = oldObjectId;
		this.newObjectId = newObjectId;
	}

	public Depot getDepot() {
		return depot;
	}

	public String getRefName() {
		return refName;
	}

	public ObjectId getOldObjectId() {
		return oldObjectId;
	}

	public ObjectId getNewObjectId() {
		return newObjectId;
	}
	
}
