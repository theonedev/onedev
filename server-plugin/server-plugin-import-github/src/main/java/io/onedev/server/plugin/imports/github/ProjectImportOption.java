package io.onedev.server.plugin.imports.github;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.jspecify.annotations.Nullable;

import io.onedev.server.OneDev;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.RoleChoice;
import io.onedev.server.service.RoleService;
import io.onedev.server.model.Role;

@Editable
public class ProjectImportOption implements Serializable {

	private static final long serialVersionUID = 1L;

	private List<String> publicRoleNames = new ArrayList<>();
	
	private IssueImportOption issueImportOption;
	
	@Editable(order=100, name="Public Roles", description="If specified, all public repositories imported from GitHub "
			+ "will use these as default roles. Private repositories are not affected")
	@RoleChoice
	@Nullable
	public List<String> getPublicRoleNames() {
		return publicRoleNames;
	}

	public void setPublicRoleNames(List<String> publicRoleNames) {
		this.publicRoleNames = publicRoleNames;
	}
	
	public List<Role> getPublicRoles() {
		return publicRoleNames.stream().map(name -> OneDev.getInstance(RoleService.class).find(name)).collect(Collectors.toList());
	}

	@Editable(order=200, name="Import Issues")
	@Nullable
	public IssueImportOption getIssueImportOption() {
		return issueImportOption;
	}

	public void setIssueImportOption(IssueImportOption issueImportOption) {
		this.issueImportOption = issueImportOption;
	}
	
}
