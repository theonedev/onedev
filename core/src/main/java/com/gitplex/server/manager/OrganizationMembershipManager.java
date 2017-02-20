package com.gitplex.server.manager;

import java.util.Collection;

import javax.annotation.Nullable;

import com.gitplex.server.model.Account;
import com.gitplex.server.model.OrganizationMembership;
import com.gitplex.server.model.TeamMembership;
import com.gitplex.server.persistence.dao.EntityManager;

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
