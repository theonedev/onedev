package com.turbodev.server.manager;

import java.util.Collection;

import com.turbodev.server.model.Membership;
import com.turbodev.server.persistence.dao.EntityManager;

public interface MembershipManager extends EntityManager<Membership> {
	
	void delete(Collection<Membership> memberships);
}
