package io.onedev.server.service.impl;

import static java.util.stream.Collectors.toSet;

import java.util.List;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import io.onedev.server.event.Listen;
import io.onedev.server.event.system.SystemStarted;
import io.onedev.server.persistence.dao.Restrictions;

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
	@Listen
	public void on(SystemStarted event) {
		var tokenValues = System.getenv("agent_tokens");
		if (tokenValues != null) {
			for (var value: tokenValues.split("\n")) {
				if (!value.isEmpty() && find(value) == null) {
					var token = new AgentToken();
					token.setValue(value);
					createOrUpdate(token);
				}
			}
		}
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
