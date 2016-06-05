package com.pmease.gitplex.core.listener;

import com.pmease.commons.loader.ExtensionPoint;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.entity.Depot;

@ExtensionPoint
public interface DepotListener {
	
	void onSaveDepot(Depot depot);
	
	void onDeleteDepot(Depot depot);

	void onRenameDepot(Depot renamedDepot, String oldName);
	
	void onTransferDepot(Depot depot, Account oldAccount);
	
}
