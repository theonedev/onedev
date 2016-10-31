package com.gitplex.web.depotaccess;

import java.io.Serializable;

import org.apache.wicket.Component;

import com.gitplex.core.security.privilege.DepotPrivilege;

public interface PrivilegeSource extends Serializable {
	
	DepotPrivilege getPrivilege();
	
	Component render(String componentId);
}