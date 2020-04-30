package io.onedev.server.entitymanager;

import java.util.Collection;

import io.onedev.server.model.Group;
import io.onedev.server.model.GroupAuthorization;
import io.onedev.server.persistence.dao.EntityManager;

public interface GroupAuthorizationManager extends EntityManager<GroupAuthorization> {

	void syncAuthorizations(Group group, Collection<GroupAuthorization> authorizations);
	
}
