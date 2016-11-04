package com.gitplex.server.core.manager;

import java.util.Collection;

import javax.annotation.Nullable;

import com.gitplex.commons.hibernate.dao.EntityManager;
import com.gitplex.server.core.entity.Account;
import com.gitplex.server.core.entity.OrganizationMembership;
import com.gitplex.server.core.entity.TeamMembership;

public interface OrganizationMembershipManager extends EntityManager<OrganizationMembership> {
	
	@Nullable
	OrganizationMembership find(Account organization, Account user);
	
	void save(Collection<OrganizationMembership> organizationMemberships, 
			Collection<TeamMembership> teamMemberships);
	
	void delete(Collection<OrganizationMembership> organizationMemberships);

	void save(OrganizationMembership organizationMembership, 
			Collection<TeamMembership> teamMembershipsToAdd, 
			Collection<TeamMembership> teamMembershipsToRemove);
	
}
