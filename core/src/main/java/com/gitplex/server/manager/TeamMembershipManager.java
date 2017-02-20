package com.gitplex.server.manager;

import java.util.Collection;

import com.gitplex.server.model.Account;
import com.gitplex.server.model.TeamMembership;
import com.gitplex.server.persistence.dao.EntityManager;

public interface TeamMembershipManager extends EntityManager<TeamMembership> {

	Collection<TeamMembership> findAll(Account organization, Account user);
	
	Collection<TeamMembership> findAll(Account organization);
	
	void delete(Collection<TeamMembership> memberships);
}
