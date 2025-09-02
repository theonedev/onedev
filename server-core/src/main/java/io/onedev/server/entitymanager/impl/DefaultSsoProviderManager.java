package io.onedev.server.entitymanager.impl;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import io.onedev.server.entitymanager.SsoProviderManager;
import io.onedev.server.model.SsoProvider;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.BaseEntityManager;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.persistence.dao.EntityCriteria;

@Singleton
public class DefaultSsoProviderManager extends BaseEntityManager<SsoProvider> implements SsoProviderManager {
	
	@Inject
	public DefaultSsoProviderManager(Dao dao) {
		super(dao);
	}

	@Transactional
	@Override
	public void createOrUpdate(SsoProvider ssoProvider) {
		dao.persist(ssoProvider);
	}
	
	@Sessional
    @Override
    public SsoProvider find(String name) {
		var criteria = EntityCriteria.of(SsoProvider.class);
		criteria.add(Restrictions.eq(SsoProvider.PROP_NAME, name));
		return dao.find(criteria);
    }

	@Sessional
	public List<SsoProvider> query() {
		var criteria = EntityCriteria.of(SsoProvider.class);
		criteria.addOrder(Order.asc(SsoProvider.PROP_NAME));
		criteria.setCacheable(true);
		return query(criteria);
	}
	
}
