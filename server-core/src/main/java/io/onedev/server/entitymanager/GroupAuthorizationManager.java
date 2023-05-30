package io.onedev.server.entitymanager;

import io.onedev.server.model.Group;
import io.onedev.server.model.GroupAuthorization;
import io.onedev.server.model.Project;
import io.onedev.server.persistence.dao.EntityManager;

import java.util.Collection;

public interface GroupAuthorizationManager extends EntityManager<GroupAuthorization> {

	void syncAuthorizations(Group group, Collection<GroupAuthorization> authorizations);

	void syncAuthorizations(Project project, Collection<GroupAuthorization> authorizations);
	
    void create(GroupAuthorization authorization);

	void update(GroupAuthorization authorization);
	
}
