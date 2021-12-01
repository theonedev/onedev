package io.onedev.server.entitymanager.impl;

import java.util.Comparator;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.onedev.server.entitymanager.LinkSpecManager;
import io.onedev.server.model.LinkSpec;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
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

	@Override
	public List<LinkSpec> queryAndSort() {
		List<LinkSpec> links = query();
		links.sort(Comparator.comparing(LinkSpec::getOrder));
		return links;
	}

	@Transactional
	@Override
	public void updateOrders(List<LinkSpec> links) {
		for (int i=0; i<links.size(); i++) {
			LinkSpec link = links.get(i);
			link.setOrder(i+1);
			save(link);
		}
	}

}
