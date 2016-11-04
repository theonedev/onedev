package com.gitplex.server.web.depotaccess;

import org.apache.wicket.Component;
import org.apache.wicket.model.LoadableDetachableModel;

import com.gitplex.server.core.GitPlex;
import com.gitplex.server.core.entity.OrganizationMembership;
import com.gitplex.server.core.manager.OrganizationMembershipManager;
import com.gitplex.server.core.security.privilege.DepotPrivilege;

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
