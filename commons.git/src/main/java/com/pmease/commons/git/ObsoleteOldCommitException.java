package com.pmease.commons.git;

import javax.annotation.Nullable;

import org.eclipse.jgit.lib.ObjectId;

public class ObsoleteOldCommitException extends GitException {

	private static final long serialVersionUID = 1L;
	
	private final ObjectId oldCommitId;
	
	public ObsoleteOldCommitException(@Nullable ObjectId oldCommitId) {
		this.oldCommitId = oldCommitId;
	}

	@Nullable
	public ObjectId getOldCommitId() {
		return oldCommitId;
	}

}
