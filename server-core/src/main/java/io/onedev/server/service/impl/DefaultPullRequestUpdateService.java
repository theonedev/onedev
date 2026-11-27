package io.onedev.server.service.impl;

import java.util.List;

import jakarta.inject.Singleton;

import io.onedev.server.persistence.dao.Order;
import io.onedev.server.persistence.dao.Restrictions;

import io.onedev.server.model.PullRequestUpdate;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.dao.EntityCriteria;
import io.onedev.server.service.PullRequestUpdateService;

@Singleton
public class DefaultPullRequestUpdateService extends BaseEntityService<PullRequestUpdate>
		implements PullRequestUpdateService {

	@Sessional
	@Override
	public List<PullRequestUpdate> queryAfter(Long projectId, Long afterUpdateId, int count) {
		EntityCriteria<PullRequestUpdate> criteria = newCriteria();
		criteria.createCriteria("request").add(Restrictions.eq("targetProject.id", projectId));
		criteria.add(Restrictions.gt("id", afterUpdateId));
		criteria.addOrder(Order.asc("id"));
		return query(criteria, 0, count);
	}

}
