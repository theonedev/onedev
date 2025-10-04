package io.onedev.server.service.impl;

import javax.inject.Singleton;

import com.google.common.base.Preconditions;

import io.onedev.server.model.AgentLastUsedDate;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.service.AgentLastUsedDateService;

@Singleton
public class DefaultAgentLastUsedDateService extends BaseEntityService<AgentLastUsedDate> implements AgentLastUsedDateService {

	@Transactional
	@Override
	public void create(AgentLastUsedDate lastUsedDate) {
		Preconditions.checkState(lastUsedDate.isNew());
		dao.persist(lastUsedDate);
	}
	
}
