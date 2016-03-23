package com.pmease.gitplex.web.depotaccess;

import org.apache.wicket.Component;
import org.apache.wicket.model.LoadableDetachableModel;

import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.entity.OrganizationMembership;
import com.pmease.gitplex.core.manager.OrganizationMembershipManager;
import com.pmease.gitplex.core.security.privilege.DepotPrivilege;

public class IsOrganizationAdmin implements PrivilegeSource {

	private static final long serialVersionUID = 1L;
	
	private final Long membershipId;
	
	public IsOrganizationAdmin(OrganizationMembership membership) {
		this.membershipId = membership.getId();
	}
	
	@Override
	public DepotPrivilege getPrivilege() {
		return DepotPrivilege.ADMIN;
	}

	@Override
	public Component render(String componentId) {
		return new IsOrganizationAdminPanel(componentId, new LoadableDetachableModel<OrganizationMembership>() {

			private static final long serialVersionUID = 1L;

			@Override
			protected OrganizationMembership load() {
				return GitPlex.getInstance(OrganizationMembershipManager.class).load(membershipId);
			}
			
		});
	}

}
