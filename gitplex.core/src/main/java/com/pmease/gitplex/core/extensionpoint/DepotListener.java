package com.pmease.gitplex.core.extensionpoint;

import com.pmease.commons.loader.ExtensionPoint;
import com.pmease.gitplex.core.entity.Depot;

@ExtensionPoint
public interface DepotListener {
	
	void beforeDelete(Depot depot);
	
	void afterDelete(Depot depot);

}
