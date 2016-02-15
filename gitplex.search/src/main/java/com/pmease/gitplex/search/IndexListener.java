package com.pmease.gitplex.search;

import com.pmease.gitplex.core.model.Depot;

public interface IndexListener {
	
	void commitIndexed(Depot depot, String revision);
	
	void indexRemoving(Depot depot);
	
}
