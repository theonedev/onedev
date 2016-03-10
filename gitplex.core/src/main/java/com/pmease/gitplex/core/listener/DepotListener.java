package com.pmease.gitplex.core.listener;

import com.pmease.commons.loader.ExtensionPoint;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.entity.Depot;

@ExtensionPoint
public interface DepotListener {
	
	void onDepotDelete(Depot depot);

	void onDepotRename(Depot renamedDepot, String oldName);
	
	void onDepotTransfer(Depot depot, Account oldOwner);
	
}
