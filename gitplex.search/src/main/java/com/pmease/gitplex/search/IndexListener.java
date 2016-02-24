package com.pmease.gitplex.search;

import com.pmease.gitplex.core.entity.Depot;

public interface IndexListener {
	
	void commitIndexed(Depot depot, String revision);
	
	void indexRemoving(Depot depot);
	
}
