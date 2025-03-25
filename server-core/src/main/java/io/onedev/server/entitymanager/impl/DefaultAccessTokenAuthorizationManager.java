package io.onedev.server.entitymanager.impl;

import io.onedev.server.entitymanager.AccessTokenAuthorizationManager;
import io.onedev.server.model.AccessToken;
import io.onedev.server.model.AccessTokenAuthorization;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.BaseEntityManager;
import io.onedev.server.persistence.dao.Dao;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collection;

@Singleton
public class DefaultAccessTokenAuthorizationManager extends BaseEntityManager<AccessTokenAuthorization> 
		implements AccessTokenAuthorizationManager {

	@Inject
	public DefaultAccessTokenAuthorizationManager(Dao dao) {
		super(dao);
	}
	
	@Transactional
	@Override
	public void syncAuthorizations(AccessToken token, Collection<AccessTokenAuthorization> authorizations) {
		for (var it = token.getAuthorizations().iterator(); it.hasNext();) {
			var authorization = it.next();
			boolean found = false;
			for (var newAuthorization: authorizations) {
				if (newAuthorization.getProject().equals(authorization.getProject()) 
						&& newAuthorization.getRole().equals(authorization.getRole())) {
					found = true;
					break;
				}
			}
			if (!found) {
				it.remove();
				delete(authorization);
			}
		}

		for (var newAuthorization: authorizations) {
			boolean found = false;
			for (var authorization: token.getAuthorizations()) {
				if (authorization.getProject().equals(newAuthorization.getProject()) 
						&& authorization.getRole().equals(newAuthorization.getRole())) {
					found = true;
					break;
				}
			}
			if (!found) {
				token.getAuthorizations().add(newAuthorization);
				dao.persist(newAuthorization);
			}
		}
	}

	@Transactional
	@Override
	public void createOrUpdate(AccessTokenAuthorization authorization) {
		dao.persist(authorization);
	}
	
}
