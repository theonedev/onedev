package io.onedev.server.entitymanager.impl;

import com.google.common.base.Preconditions;
import io.onedev.server.entitymanager.AgentLastUsedDateManager;
import io.onedev.server.model.AgentLastUsedDate;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.BaseEntityManager;
import io.onedev.server.persistence.dao.Dao;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class DefaultAgentLastUsedDateManager extends BaseEntityManager<AgentLastUsedDate> implements AgentLastUsedDateManager {

	@Inject
	public DefaultAgentLastUsedDateManager(Dao dao) {
		super(dao);
	}

	@Transactional
	@Override
	public void create(AgentLastUsedDate lastUsedDate) {
		Preconditions.checkState(lastUsedDate.isNew());
		dao.persist(lastUsedDate);
	}
	
}
