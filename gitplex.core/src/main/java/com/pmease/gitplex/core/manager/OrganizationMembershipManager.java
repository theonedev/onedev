package com.pmease.gitplex.core.manager;

import java.util.Collection;

import javax.annotation.Nullable;

import com.pmease.commons.hibernate.dao.EntityDao;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.entity.OrganizationMembership;
import com.pmease.gitplex.core.entity.TeamMembership;

public interface OrganizationMembershipManager extends EntityDao<OrganizationMembership> {
	
	void save(OrganizationMembership membership);
	
	@Nullable
	OrganizationMembership find(Account organization, Account user);
	
	void save(Collection<OrganizationMembership> organizationMemberships, 
			Collection<TeamMembership> teamMemberships);
	
	void delete(Collection<OrganizationMembership> organizationMemberships, 
			Collection<TeamMembership> teamMemberships);

}
