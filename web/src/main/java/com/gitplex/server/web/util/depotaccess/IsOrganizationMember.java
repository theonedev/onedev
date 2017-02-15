package com.gitplex.server.web.util.depotaccess;

import org.apache.wicket.Component;
import org.apache.wicket.model.LoadableDetachableModel;

import com.gitplex.server.GitPlex;
import com.gitplex.server.entity.OrganizationMembership;
import com.gitplex.server.manager.OrganizationMembershipManager;
import com.gitplex.server.security.privilege.DepotPrivilege;

public class IsOrganizationMember implements PrivilegeSource {

	private static final long serialVersionUID = 1L;
	
	private final Long membershipId;
	
	private final DepotPrivilege privilege;
	
	public IsOrganizationMember(OrganizationMembership membership) {
		this.membershipId = membership.getId();
		this.privilege = membership.getOrganization().getDefaultPrivilege();
	}
	
	@Override
	public DepotPrivilege getPrivilege() {
		return privilege;
	}

	@Override
	public Component render(String componentId) {
		return new IsOrganizationMemberPanel(componentId, new LoadableDetachableModel<OrganizationMembership>() {

			private static final long serialVersionUID = 1L;

			@Override
			protected OrganizationMembership load() {
				return GitPlex.getInstance(OrganizationMembershipManager.class).load(membershipId);
			}
			
		});
	}

}
