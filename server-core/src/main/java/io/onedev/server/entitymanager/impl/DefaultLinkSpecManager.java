package io.onedev.server.entitymanager.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.onedev.server.entitymanager.LinkSpecManager;
import io.onedev.server.model.LinkSpec;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.dao.BaseEntityManager;
import io.onedev.server.persistence.dao.Dao;

@Singleton
public class DefaultLinkSpecManager extends BaseEntityManager<LinkSpec> implements LinkSpecManager {

	@Inject
	public DefaultLinkSpecManager(Dao dao) {
		super(dao);
	}

	@Sessional
	@Override
	public LinkSpec find(String name) {
		for (LinkSpec spec: query()) {
			if (spec.getName().equals(name) 
					|| spec.getOpposite() != null && spec.getOpposite().getName().equalsIgnoreCase(name)) {
				return spec;
			}
		}
		return null;
	}

}
