package io.onedev.server.entitymanager.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.criterion.Restrictions;

import io.onedev.server.entitymanager.SsoAccountManager;
import io.onedev.server.model.SsoAccount;
import io.onedev.server.model.SsoProvider;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.BaseEntityManager;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.persistence.dao.EntityCriteria;

@Singleton
public class DefaultSsoAccountManager extends BaseEntityManager<SsoAccount> implements SsoAccountManager {
	
	@Inject
	public DefaultSsoAccountManager(Dao dao) {
		super(dao);
	}

	@Transactional
	@Override
	public void create(SsoAccount ssoAccount) {
		dao.persist(ssoAccount);
	}

	@Sessional
	@Override
	public SsoAccount find(SsoProvider provider, String subject) {
		var criteria = EntityCriteria.of(SsoAccount.class);
		criteria.add(Restrictions.eq(SsoAccount.PROP_PROVIDER, provider));
		criteria.add(Restrictions.eq(SsoAccount.PROP_SUBJECT, subject));
		return dao.find(criteria);
	}
	
}
