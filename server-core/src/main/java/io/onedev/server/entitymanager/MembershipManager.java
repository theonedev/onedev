package io.onedev.server.entitymanager;

import io.onedev.server.model.Membership;
import io.onedev.server.model.User;
import io.onedev.server.persistence.dao.EntityManager;

import java.util.Collection;
import java.util.List;

public interface MembershipManager extends EntityManager<Membership> {
	
	void delete(Collection<Membership> memberships);

	void syncMemberships(User user, Collection<String> groupNames);

    void create(Membership membership);

    List<User> queryMembers(User user);
	
}
