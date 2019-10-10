package io.onedev.server.util;

import org.eclipse.jgit.lib.ObjectId;

public interface BuildCommitAware {
	
	ObjectId getBuildCommit();
	
}
