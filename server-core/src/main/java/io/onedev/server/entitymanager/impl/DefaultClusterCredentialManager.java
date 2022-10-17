package io.onedev.server.entitymanager.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.onedev.server.entitymanager.ClusterCredentialManager;
import io.onedev.server.model.ClusterCredential;
import io.onedev.server.persistence.dao.BaseEntityManager;
import io.onedev.server.persistence.dao.Dao;

@Singleton
public class DefaultClusterCredentialManager extends BaseEntityManager<ClusterCredential> 
		implements ClusterCredentialManager {

	@Inject
	public DefaultClusterCredentialManager(Dao dao) {
		super(dao);
	}

}
