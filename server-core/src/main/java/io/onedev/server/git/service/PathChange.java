package io.onedev.server.git.service;

import java.io.Serializable;

import org.eclipse.jgit.lib.ObjectId;

public class PathChange implements Serializable {

	private static final long serialVersionUID = 1L;

	private final ObjectId oldObjectId;
	
	private final ObjectId newObjectId;
	
	private final int oldMode;
	
	private final int newMode;
	
	public PathChange(ObjectId oldObjectId, ObjectId newObjectid, 
			int oldMode, int newMode) {
		this.oldObjectId = oldObjectId;
		this.newObjectId = newObjectid;
		this.oldMode = oldMode;
		this.newMode = newMode;
	}

	public ObjectId getOldObjectId() {
		return oldObjectId;
	}

	public ObjectId getNewObjectId() {
		return newObjectId;
	}

	public int getOldMode() {
		return oldMode;
	}

	public int getNewMode() {
		return newMode;
	}
	
}
