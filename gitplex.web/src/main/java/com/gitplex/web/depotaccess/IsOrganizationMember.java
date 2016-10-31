package com.gitplex.web.depotaccess;

import org.apache.wicket.Component;
import org.apache.wicket.model.LoadableDetachableModel;

import com.gitplex.core.GitPlex;
import com.gitplex.core.entity.OrganizationMembership;
import com.gitplex.core.manager.OrganizationMembershipManager;
import com.gitplex.core.security.privilege.DepotPrivilege;

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
