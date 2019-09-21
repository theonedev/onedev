package io.onedev.server.event;

import org.eclipse.jgit.lib.ObjectId;

import io.onedev.server.model.Project;
import io.onedev.server.util.CommitAware;

public class RefUpdated extends ProjectEvent implements CommitAware {
	
	private final String refName;
	
	private final ObjectId oldCommitId;
	
	private final ObjectId newCommitId;
	
	public RefUpdated(Project project, String refName, ObjectId oldCommitId, ObjectId newCommitId) {
		super(null, null, project);
		this.refName = refName;
		this.oldCommitId = oldCommitId;
		this.newCommitId = newCommitId;
	}

	public String getRefName() {
		return refName;
	}

	public ObjectId getOldCommitId() {
		return oldCommitId;
	}

	public ObjectId getNewCommitId() {
		return newCommitId;
	}

	@Override
	public ObjectId getCommitId() {
		return newCommitId;
	}
	
}
