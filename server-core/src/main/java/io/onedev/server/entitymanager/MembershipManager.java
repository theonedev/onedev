package io.onedev.server.entitymanager;

import java.util.Collection;

import io.onedev.server.model.Membership;
import io.onedev.server.model.User;
import io.onedev.server.persistence.dao.EntityManager;

public interface MembershipManager extends EntityManager<Membership> {
	
	void delete(Collection<Membership> memberships);

	void syncMemberships(User user, Collection<String> groupNames);
	
}
