package com.gitplex.server.model.support.tagcreator;

import com.gitplex.server.model.Account;
import com.gitplex.server.model.Depot;
import com.gitplex.server.security.ObjectPermission;
import com.gitplex.server.util.editable.annotation.Editable;

@Editable(order=100, name="All Users Able to Write to the Repository")
public class DepotWriters implements TagCreator {

	private static final long serialVersionUID = 1L;

	@Override
	public String getNotMatchMessage(Depot depot, Account user) {
		if (!user.asSubject().isPermitted(ObjectPermission.ofDepotWrite(depot))) {
			return "This operation can only be performed by repository writers";
		} else {
			return null;
		}
	}

}
