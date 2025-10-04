package io.onedev.server.service;

import io.onedev.server.model.Group;
import io.onedev.server.model.GroupAuthorization;
import io.onedev.server.model.Project;

import java.util.Collection;

public interface GroupAuthorizationService extends EntityService<GroupAuthorization> {

	void syncAuthorizations(Group group, Collection<GroupAuthorization> authorizations);

	void syncAuthorizations(Project project, Collection<GroupAuthorization> authorizations);
	
    void createOrUpdate(GroupAuthorization authorization);
	
}
