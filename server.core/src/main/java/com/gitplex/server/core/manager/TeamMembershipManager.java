package com.gitplex.server.core.manager;

import java.util.Collection;

import com.gitplex.commons.hibernate.dao.EntityManager;
import com.gitplex.server.core.entity.Account;
import com.gitplex.server.core.entity.TeamMembership;

public interface TeamMembershipManager extends EntityManager<TeamMembership> {

	Collection<TeamMembership> findAll(Account organization, Account user);
	
	Collection<TeamMembership> findAll(Account organization);
	
	void delete(Collection<TeamMembership> memberships);
}
