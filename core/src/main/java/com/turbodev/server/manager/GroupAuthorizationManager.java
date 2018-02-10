package com.turbodev.server.manager;

import java.util.Collection;

import com.turbodev.server.model.GroupAuthorization;
import com.turbodev.server.persistence.dao.EntityManager;

public interface GroupAuthorizationManager extends EntityManager<GroupAuthorization> {
	
	void delete(Collection<GroupAuthorization> authorizations);
	
}
