package io.onedev.server.plugin.imports.bitbucketcloud;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import io.onedev.server.OneDev;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.RoleChoice;
import io.onedev.server.service.RoleService;
import io.onedev.server.model.Role;

@Editable
public class ImportOption implements Serializable {

	private static final long serialVersionUID = 1L;

	private List<String> publicRoleNames = new ArrayList<>();
	
	@Editable(order=100, name="Public Roles", description="If specified, all public repositories imported from GitHub "
			+ "will use these as default roles. Private repositories are not affected")
	@RoleChoice
	public List<String> getPublicRoleNames() {
		return publicRoleNames;
	}

	public void setPublicRoleNames(List<String> publicRoleNames) {
		this.publicRoleNames = publicRoleNames;
	}
	
	public List<Role> getPublicRoles() {
		return publicRoleNames.stream().map(name -> OneDev.getInstance(RoleService.class).find(name)).collect(Collectors.toList());
	}

}
