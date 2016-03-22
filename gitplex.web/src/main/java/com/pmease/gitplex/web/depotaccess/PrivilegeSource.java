package com.pmease.gitplex.web.depotaccess;

import org.apache.wicket.Component;

import com.pmease.gitplex.core.security.privilege.DepotPrivilege;

public interface PrivilegeSource {
	
	DepotPrivilege getPrivilege();
	
	Component render(String componentId);
}