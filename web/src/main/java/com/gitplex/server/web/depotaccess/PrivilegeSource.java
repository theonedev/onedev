package com.gitplex.server.web.depotaccess;

import java.io.Serializable;

import org.apache.wicket.Component;

import com.gitplex.server.core.security.privilege.DepotPrivilege;

public interface PrivilegeSource extends Serializable {
	
	DepotPrivilege getPrivilege();
	
	Component render(String componentId);
}