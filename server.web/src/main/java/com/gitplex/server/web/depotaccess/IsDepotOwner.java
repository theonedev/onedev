package com.gitplex.server.web.depotaccess;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;

import com.gitplex.server.core.security.privilege.DepotPrivilege;

public class IsDepotOwner implements PrivilegeSource {

	private static final long serialVersionUID = 1L;
	
	@Override
	public DepotPrivilege getPrivilege() {
		return DepotPrivilege.ADMIN;
	}

	@Override
	public Component render(String componentId) {
		return new Label(componentId, "This user is owner of this repository");
	}

}
