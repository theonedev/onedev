package com.gitplex.server.model.support.tagcreator;

import com.gitplex.server.model.Account;
import com.gitplex.server.model.Depot;
import com.gitplex.server.security.ObjectPermission;
import com.gitplex.server.util.editable.annotation.Editable;

@Editable(order=200, name="Repository Administrators")
public class DepotAdministrators implements TagCreator {

	private static final long serialVersionUID = 1L;

	@Override
	public String getNotMatchMessage(Depot depot, Account user) {
		if (!user.asSubject().isPermitted(ObjectPermission.ofDepotAdmin(depot))) {
			return "This operation can only be performed by repository administrators";
		} else {
			return null;
		}
	}

}
