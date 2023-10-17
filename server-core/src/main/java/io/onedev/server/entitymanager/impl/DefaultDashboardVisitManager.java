package io.onedev.server.entitymanager.impl;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.common.base.Preconditions;
import io.onedev.server.entitymanager.DashboardVisitManager;
import io.onedev.server.model.DashboardVisit;
import io.onedev.server.persistence.dao.BaseEntityManager;
import io.onedev.server.persistence.dao.Dao;

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
	
	@Override
	public void create(DashboardVisit visit) {
		Preconditions.checkState(visit.isNew());
		dao.persist(visit);
	}

	@Override
	public void update(DashboardVisit visit) {
		Preconditions.checkState(!visit.isNew());
		dao.persist(visit);
	}
	
}
