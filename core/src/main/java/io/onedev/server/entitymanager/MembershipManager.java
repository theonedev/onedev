package io.onedev.server.entitymanager;

import java.util.Collection;

import io.onedev.server.model.Membership;
import io.onedev.server.persistence.dao.EntityManager;

public interface MembershipManager extends EntityManager<Membership> {
	
	void delete(Collection<Membership> memberships);

}
