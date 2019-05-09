package io.onedev.server.event;

import java.util.Collection;

import org.eclipse.jgit.lib.ObjectId;

public interface BuildCommitsAware {
	
	Collection<ObjectId> getBuildCommits();
	
}
