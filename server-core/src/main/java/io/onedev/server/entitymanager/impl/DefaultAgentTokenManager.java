package io.onedev.server.entitymanager.impl;

import com.google.common.base.Preconditions;
import io.onedev.server.entitymanager.AgentManager;
import io.onedev.server.entitymanager.AgentTokenManager;
import io.onedev.server.model.Agent;
import io.onedev.server.model.AgentToken;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.BaseEntityManager;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.persistence.dao.EntityCriteria;
import org.hibernate.criterion.Restrictions;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

import static java.util.stream.Collectors.toSet;

@Singleton
public class DefaultAgentTokenManager extends BaseEntityManager<AgentToken> implements AgentTokenManager {

	private final AgentManager agentManager;
	
	@Inject
	public DefaultAgentTokenManager(Dao dao, AgentManager agentManager) {
		super(dao);
		this.agentManager = agentManager;
	}

	@Override
	public void create(AgentToken token) {
		Preconditions.checkState(token.isNew());
		dao.persist(token);
	}

	@Override
	public void update(AgentToken token) {
		Preconditions.checkState(!token.isNew());
		dao.persist(token);
	}
	
	@Override
	public AgentToken find(String value) {
		EntityCriteria<AgentToken> criteria = newCriteria();
		criteria.add(Restrictions.eq(AgentToken.PROP_VALUE, value));
		criteria.setCacheable(true);
		return find(criteria);
	}

	@SuppressWarnings("unchecked")
	@Sessional
	@Override
	public List<AgentToken> queryUnused() {
		var tokens = query();
		var usedTokens = agentManager.query().stream().map(Agent::getToken).collect(toSet());
		tokens.removeAll(usedTokens);
		return tokens;
	}

	@SuppressWarnings("unchecked")
	@Transactional
	@Override
	public void deleteUnused() {
		for (var token: queryUnused())
			delete(token);
	}

}
