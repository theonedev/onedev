package com.gitplex.server.manager;

import java.util.Collection;

import com.gitplex.server.model.Membership;
import com.gitplex.server.persistence.dao.EntityManager;

public interface MembershipManager extends EntityManager<Membership> {
	
	void delete(Collection<Membership> memberships);
}
