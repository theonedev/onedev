package com.pmease.gitplex.web.component.diff;

import com.pmease.commons.git.Change;

@SuppressWarnings("serial")
public class BlobDiffInfo extends Change {
	
	private final String oldRevision;
	
	private final String newRevision;
	
	public BlobDiffInfo(Change change, String oldRevision, String newRevision) {
		super(change);
		this.oldRevision = oldRevision;
		this.newRevision = newRevision;
	}
	
	public BlobDiffInfo(Status status, String oldPath, String newPath, int oldMode, int newMode, 
			String oldRevision, String newRevision) {
		super(status, oldPath, newPath, oldMode, newMode);
		this.oldRevision = oldRevision;
		this.newRevision = newRevision;
	}

	public String getOldRevision() {
		return oldRevision;
	}

	public String getNewRevision() {
		return newRevision;
	}

}