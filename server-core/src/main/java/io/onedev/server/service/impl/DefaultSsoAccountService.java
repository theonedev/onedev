package io.onedev.server.service.impl;

import javax.inject.Singleton;

import org.hibernate.criterion.Restrictions;

import io.onedev.server.model.SsoAccount;
import io.onedev.server.model.SsoProvider;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.EntityCriteria;
import io.onedev.server.service.SsoAccountService;

@Singleton
public class DefaultSsoAccountService extends BaseEntityService<SsoAccount> implements SsoAccountService {

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
