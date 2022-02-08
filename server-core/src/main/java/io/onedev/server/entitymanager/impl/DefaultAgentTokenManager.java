package io.onedev.server.entitymanager.impl;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.criterion.Restrictions;

import io.onedev.server.entitymanager.AgentTokenManager;
import io.onedev.server.model.AgentToken;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.BaseEntityManager;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.persistence.dao.EntityCriteria;

@Singleton
public class DefaultAgentTokenManager extends BaseEntityManager<AgentToken> implements AgentTokenManager {

	@Inject
	public DefaultAgentTokenManager(Dao dao) {
		super(dao);
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
		return getSession().createQuery("select token from AgentToken token left join token.agent agent where agent = null").list();
	}

	@SuppressWarnings("unchecked")
	@Transactional
	@Override
	public void deleteUnused() {
		for (AgentToken token: (List<AgentToken>)getSession().createQuery("select token from AgentToken token left join token.agent agent where agent = null").list()) {
			delete(token);
		}
	}

}
