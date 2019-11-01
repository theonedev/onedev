package io.onedev.server.entitymanager.impl;

import java.util.Collection;
import java.util.Iterator;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.onedev.server.entitymanager.GroupAuthorizationManager;
import io.onedev.server.model.Group;
import io.onedev.server.model.GroupAuthorization;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.AbstractEntityManager;
import io.onedev.server.persistence.dao.Dao;

@Singleton
public class DefaultGroupAuthorizationManager extends AbstractEntityManager<GroupAuthorization> 
		implements GroupAuthorizationManager {

	@Inject
	public DefaultGroupAuthorizationManager(Dao dao) {
		super(dao);
	}

	@Transactional
	@Override
	public void authorize(Group group, Collection<GroupAuthorization> authorizations) {
		for (Iterator<GroupAuthorization> it = group.getAuthorizations().iterator(); it.hasNext();) {
			GroupAuthorization authorization = it.next();
			boolean found = false;
			for (GroupAuthorization newAuthorization: authorizations) {
				if (newAuthorization.getProject().equals(authorization.getProject())) {
					found = true;
					authorization.setRole(newAuthorization.getRole());
					save(authorization);
				}
			}
			if (!found) {
				it.remove();
				delete(authorization);
			}
		}

		for (GroupAuthorization newAuthorization: authorizations) {
			boolean found = false;
			for (GroupAuthorization authorization: group.getAuthorizations()) {
				if (authorization.getProject().equals(newAuthorization.getProject())) {
					found = true;
					break;
				}
			}
			if (!found) {
				group.getAuthorizations().add(newAuthorization);
				save(newAuthorization);
			}
		}
	}
	
}
