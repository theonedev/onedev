package io.onedev.server.entitymanager.impl;

import static java.util.stream.Collectors.toSet;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.criterion.Restrictions;

import io.onedev.server.entitymanager.AgentManager;
import io.onedev.server.entitymanager.AgentTokenManager;
import io.onedev.server.model.Agent;
import io.onedev.server.model.AgentToken;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.BaseEntityManager;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.persistence.dao.EntityCriteria;

@Singleton
public class DefaultAgentTokenManager extends BaseEntityManager<AgentToken> implements AgentTokenManager {

	private final AgentManager agentManager;
	
	@Inject
	public DefaultAgentTokenManager(Dao dao, AgentManager agentManager) {
		super(dao);
		this.agentManager = agentManager;
	}

	@Transactional
	@Override
	public void createOrUpdate(AgentToken token) {
		dao.persist(token);
	}

	@Override
	public AgentToken find(String value) {
		EntityCriteria<AgentToken> criteria = newCriteria();
		criteria.add(Restrictions.eq(AgentToken.PROP_VALUE, value));
		criteria.setCacheable(true);
		return find(criteria);
	}

	@Sessional
	@Override
	public List<AgentToken> queryUnused() {
		var tokens = query();
		var usedTokens = agentManager.query().stream().map(Agent::getToken).collect(toSet());
		tokens.removeAll(usedTokens);
		return tokens;
	}

	@Transactional
	@Override
	public void deleteUnused() {
		for (var token: queryUnused())
			delete(token);
	}

}
