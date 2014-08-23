package com.pmease.commons.git;

import javax.annotation.Nullable;

@SuppressWarnings("serial")
public class RevAwareChange extends Change {
	
	private final String oldRevision;
	
	private final String newRevision;
	
	public RevAwareChange(Change change, @Nullable String oldRevision, @Nullable String newRevision) {
		super(change);
		this.oldRevision = oldRevision;
		this.newRevision = newRevision;
	}
	
	public RevAwareChange(Status status, @Nullable String oldPath, @Nullable String newPath, int oldMode, int newMode, 
			@Nullable String oldRevision, @Nullable String newRevision) {
		super(status, oldPath, newPath, oldMode, newMode);
		this.oldRevision = oldRevision;
		this.newRevision = newRevision;
	}

	@Nullable
	public String getOldRevision() {
		return oldRevision;
	}

	@Nullable
	public String getNewRevision() {
		return newRevision;
	}

	public BlobInfo getOldBlobInfo() {
		return new BlobInfo(oldRevision, getOldPath(), getOldMode());
	}
	
	public BlobInfo getNewBlobInfo() {
		return new BlobInfo(newRevision, getNewPath(), getNewMode());
	}
	
}