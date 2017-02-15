package com.gitplex.server.manager;

import java.util.Collection;

import com.gitplex.server.entity.Account;
import com.gitplex.server.entity.TeamMembership;
import com.gitplex.server.persistence.dao.EntityManager;

public interface TeamMembershipManager extends EntityManager<TeamMembership> {

	Collection<TeamMembership> findAll(Account organization, Account user);
	
	Collection<TeamMembership> findAll(Account organization);
	
	void delete(Collection<TeamMembership> memberships);
}
