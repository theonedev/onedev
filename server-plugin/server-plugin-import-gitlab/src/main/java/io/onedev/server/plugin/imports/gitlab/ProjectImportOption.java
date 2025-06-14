package io.onedev.server.plugin.imports.gitlab;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import io.onedev.server.OneDev;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.RoleChoice;
import io.onedev.server.entitymanager.RoleManager;
import io.onedev.server.model.Role;

@Editable
public class ProjectImportOption implements Serializable {

	private static final long serialVersionUID = 1L;

	private List<String> publicRoleNames = new ArrayList<>();
	
	private IssueImportOption issueImportOption;
	
	@Editable(order=100, name="Public Roles", description="If specified, all public and internal projects imported from GitLab "
			+ "will use these as default roles. Private projects are not affected")
	@RoleChoice
	public List<String> getPublicRoleNames() {
		return publicRoleNames;
	}

	public void setPublicRoleNames(List<String> publicRoleNames) {
		this.publicRoleNames = publicRoleNames;
	}
	
	public List<Role> getPublicRoles() {
		return publicRoleNames.stream().map(name -> OneDev.getInstance(RoleManager.class).find(name)).collect(Collectors.toList());
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
