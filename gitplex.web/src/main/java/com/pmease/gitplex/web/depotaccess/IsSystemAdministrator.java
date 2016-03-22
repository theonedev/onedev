package com.pmease.gitplex.web.depotaccess;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;

import com.pmease.gitplex.core.security.privilege.DepotPrivilege;

public class IsSystemAdministrator implements PrivilegeSource {

	@Override
	public DepotPrivilege getPrivilege() {
		return DepotPrivilege.ADMIN;
	}

	@Override
	public Component render(String componentId) {
		return new Label(componentId, "This user is system administrator");
	}

}
