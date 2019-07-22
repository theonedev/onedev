package io.onedev.server.git.exception;

import javax.annotation.Nullable;

import org.eclipse.jgit.lib.ObjectId;

public class ObsoleteCommitException extends GitException {

	private static final long serialVersionUID = 1L;
	
	private final ObjectId oldCommitId;
	
	public ObsoleteCommitException(@Nullable ObjectId oldCommitId) {
		super("Commit is obsolete");
		this.oldCommitId = oldCommitId;
	}

	@Nullable
	public ObjectId getOldCommitId() {
		return oldCommitId;
	}

}
