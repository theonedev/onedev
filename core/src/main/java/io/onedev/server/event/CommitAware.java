package io.onedev.server.event;

import org.eclipse.jgit.lib.ObjectId;

public interface CommitAware {
	
	ObjectId getCommitId();
	
}
