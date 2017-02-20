package com.gitplex.server.manager;

import java.util.Collection;

import org.eclipse.jgit.lib.ObjectId;

import com.gitplex.server.model.Depot;

public interface PullRequestInfoManager {
	
	void collect(Depot depot);
	
	Collection<String> getRequests(Depot depot, ObjectId commit);
	
	void removeRequest(Depot depot, Collection<ObjectId> commits, String request);
	
}
