package com.gitplex.core.manager;

import java.util.Collection;

import com.gitplex.core.entity.Account;
import com.gitplex.core.entity.TeamMembership;
import com.gitplex.commons.hibernate.dao.EntityManager;

public interface TeamMembershipManager extends EntityManager<TeamMembership> {

	Collection<TeamMembership> findAll(Account organization, Account user);
	
	Collection<TeamMembership> findAll(Account organization);
	
	void delete(Collection<TeamMembership> memberships);
}
