package com.gitplex.server.web.util.depotaccess;

import java.io.Serializable;

import org.apache.wicket.Component;

import com.gitplex.server.security.privilege.DepotPrivilege;

public interface PrivilegeSource extends Serializable {
	
	DepotPrivilege getPrivilege();
	
	Component render(String componentId);
}