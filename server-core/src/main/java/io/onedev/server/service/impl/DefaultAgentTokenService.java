package io.onedev.server.service.impl;

import static java.util.stream.Collectors.toSet;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.criterion.Restrictions;

import io.onedev.server.model.Agent;
import io.onedev.server.model.AgentToken;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.EntityCriteria;
import io.onedev.server.service.AgentService;
import io.onedev.server.service.AgentTokenService;

@Singleton
public class DefaultAgentTokenService extends BaseEntityService<AgentToken> implements AgentTokenService {

	@Inject
	private AgentService agentService;

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
		var usedTokens = agentService.query().stream().map(Agent::getToken).collect(toSet());
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
