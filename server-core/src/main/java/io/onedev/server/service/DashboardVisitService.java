package io.onedev.server.service;

import io.onedev.server.model.DashboardVisit;

public interface DashboardVisitService extends EntityService<DashboardVisit> {

	void createOrUpdate(DashboardVisit visit);

}