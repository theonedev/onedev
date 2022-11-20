package io.onedev.server.entitymanager.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.onedev.server.entitymanager.ClusterServerManager;
import io.onedev.server.model.ClusterServer;
import io.onedev.server.persistence.dao.BaseEntityManager;
import io.onedev.server.persistence.dao.Dao;

@Singleton
public class DefaultClusterServerManager extends BaseEntityManager<ClusterServer> 
		implements ClusterServerManager {

	@Inject
	public DefaultClusterServerManager(Dao dao) {
		super(dao);
	}

}
