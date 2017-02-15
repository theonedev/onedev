package com.gitplex.server.web.util.depotaccess;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;

import com.gitplex.server.security.privilege.DepotPrivilege;

public class IsSystemAdministrator implements PrivilegeSource {

	private static final long serialVersionUID = 1L;
	
	@Override
	public DepotPrivilege getPrivilege() {
		return DepotPrivilege.ADMIN;
	}

	@Override
	public Component render(String componentId) {
		return new Label(componentId, "This user is system administrator");
	}

}
