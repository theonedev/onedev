package io.onedev.server.entitymanager;

import io.onedev.server.model.DashboardVisit;
import io.onedev.server.persistence.dao.EntityManager;

public interface DashboardVisitManager extends EntityManager<DashboardVisit> {

	void create(DashboardVisit visit);

	void update(DashboardVisit visit);
}