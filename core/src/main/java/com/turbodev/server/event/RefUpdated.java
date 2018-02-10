package com.turbodev.server.event;

import org.eclipse.jgit.lib.ObjectId;

import com.turbodev.server.model.Project;

public class RefUpdated {
	
	private final Project project;
	
	private final String refName;
	
	private final ObjectId oldObjectId;
	
	private final ObjectId newObjectId;
	
	public RefUpdated(Project project, String refName, ObjectId oldObjectId, ObjectId newObjectId) {
		this.project = project;
		this.refName = refName;
		this.oldObjectId = oldObjectId;
		this.newObjectId = newObjectId;
	}

	public Project getProject() {
		return project;
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
