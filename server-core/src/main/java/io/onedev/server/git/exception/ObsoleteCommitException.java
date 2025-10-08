package io.onedev.server.git.exception;

import org.jspecify.annotations.Nullable;

import io.onedev.commons.utils.ExplicitException;
import org.eclipse.jgit.lib.ObjectId;

public class ObsoleteCommitException extends ExplicitException {

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
