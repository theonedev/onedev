package io.onedev.server.entitymanager.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.onedev.server.entitymanager.ClusterServerManager;
import io.onedev.server.model.ClusterServer;
import io.onedev.server.persistence.dao.BaseEntityManager;
import io.onedev.server.persistence.dao.Dao;

@Singleton
public class DefaultClusterMemberManager extends BaseEntityManager<ClusterServer> 
		implements ClusterServerManager {

	@Inject
	public DefaultClusterMemberManager(Dao dao) {
		super(dao);
	}

}
