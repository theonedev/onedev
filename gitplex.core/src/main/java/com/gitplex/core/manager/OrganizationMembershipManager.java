package com.gitplex.core.manager;

import java.util.Collection;

import javax.annotation.Nullable;

import com.gitplex.core.entity.Account;
import com.gitplex.core.entity.OrganizationMembership;
import com.gitplex.core.entity.TeamMembership;
import com.gitplex.commons.hibernate.dao.EntityManager;

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
