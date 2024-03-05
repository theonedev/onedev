package io.onedev.server.entitymanager.impl;

import io.onedev.server.entitymanager.DashboardVisitManager;
import io.onedev.server.model.DashboardVisit;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.BaseEntityManager;
import io.onedev.server.persistence.dao.Dao;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

@Singleton
public class DefaultDashboardVisitManager extends BaseEntityManager<DashboardVisit> 
		implements DashboardVisitManager {

	@Inject
	public DefaultDashboardVisitManager(Dao dao) {
		super(dao);
	}
	
	@Override
	public List<DashboardVisit> query() {
		return query(true);
	}

	@Override
	public int count() {
		return count(true);
	}
	
	@Transactional
	@Override
	public void createOrUpdate(DashboardVisit visit) {
		dao.persist(visit);
	}
	
}
