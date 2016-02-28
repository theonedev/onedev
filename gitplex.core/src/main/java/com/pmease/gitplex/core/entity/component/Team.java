package com.pmease.gitplex.core.entity.component;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Team implements Serializable {

	private static final long serialVersionUID = 1L;

	private String name;
	
	private String description;

	private List<DepotAuthorization> depotAuthorizations = new ArrayList<>();

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

	public List<DepotAuthorization> getDepotAuthorizations() {
		return depotAuthorizations;
	}

	public void setDepotAuthorizations(List<DepotAuthorization> depotAuthorizations) {
		this.depotAuthorizations = depotAuthorizations;
	}
	
}
