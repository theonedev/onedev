package io.onedev.server.entitymanager.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.criterion.Restrictions;

import io.onedev.server.entitymanager.LinkSpecManager;
import io.onedev.server.model.LinkSpec;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.dao.BaseEntityManager;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.persistence.dao.EntityCriteria;

@Singleton
public class DefaultLinkSpecManager extends BaseEntityManager<LinkSpec> implements LinkSpecManager {

	@Inject
	public DefaultLinkSpecManager(Dao dao) {
		super(dao);
	}

	@Sessional
	@Override
	public LinkSpec find(String name) {
		EntityCriteria<LinkSpec> criteria = newCriteria();
		criteria.add(Restrictions.ilike("name", name));
		criteria.setCacheable(true);
		return find(criteria);
	}

}
