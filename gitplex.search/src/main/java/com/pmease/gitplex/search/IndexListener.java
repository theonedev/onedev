package com.pmease.gitplex.search;

import org.eclipse.jgit.lib.ObjectId;

import com.pmease.gitplex.core.entity.Depot;

public interface IndexListener {
	
	void commitIndexed(Depot depot, ObjectId commit);
	
}
