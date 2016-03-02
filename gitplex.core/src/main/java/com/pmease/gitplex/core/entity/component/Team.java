package com.pmease.gitplex.core.entity.component;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

import com.pmease.gitplex.core.permission.privilege.DepotPrivilege;

/**
 * Team and its authorizations are designed as serializable objects living in 
 * organization object as otherwise checking permission on an user requires 
 * 1+N database calls if the user belongs to N teams.
 *
 */
public class Team implements Serializable {

	private static final long serialVersionUID = 1L;

	private String name;
	
	private String description;

	private Map<String, DepotPrivilege> authorizations = new LinkedHashMap<>();

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Map<String, DepotPrivilege> getAuthorizations() {
		return authorizations;
	}

	public void setAuthorizations(Map<String, DepotPrivilege> authorizations) {
		this.authorizations = authorizations;
	}

}
