package com.pmease.gitplex.core.entity.component;

import java.io.Serializable;

import com.pmease.gitplex.core.permission.privilege.DepotPrivilege;

public class DepotAuthorization implements Serializable {

	private static final long serialVersionUID = 1L;

	private String depotName;
	
	private DepotPrivilege depotPrivilege;

	public String getDepotName() {
		return depotName;
	}

	public void setDepotName(String depotName) {
		this.depotName = depotName;
	}

	public DepotPrivilege getDepotPrivilege() {
		return depotPrivilege;
	}

	public void setDepotPrivilege(DepotPrivilege depotPrivilege) {
		this.depotPrivilege = depotPrivilege;
	}
	
}
