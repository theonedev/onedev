package com.pmease.gitplex.core.listeners;

import com.pmease.commons.loader.ExtensionPoint;
import com.pmease.gitplex.core.model.Depot;

@ExtensionPoint
public interface DepotListener {
	
	void beforeDelete(Depot depot);
	
	void afterDelete(Depot depot);

}
