package com.pmease.gitplex.core.entity.component;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

import org.hibernate.validator.constraints.NotEmpty;

import com.pmease.commons.wicket.editable.annotation.Editable;
import com.pmease.commons.wicket.editable.annotation.Markdown;
import com.pmease.gitplex.core.permission.privilege.DepotPrivilege;

/**
 * Team and its authorizations are designed as serializable objects living in 
 * organization object as otherwise checking permission on an user requires 
 * 1+N database calls if the user belongs to N teams.
 *
 */
@Editable
public class Team implements Serializable {

	private static final long serialVersionUID = 1L;

	private String name;
	
	private String description;

	private Map<String, DepotPrivilege> authorizations = new LinkedHashMap<>();

	@Editable(order=100)
	@NotEmpty
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Editable(order=200, description="Optionally describe the team")
	@Markdown
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
