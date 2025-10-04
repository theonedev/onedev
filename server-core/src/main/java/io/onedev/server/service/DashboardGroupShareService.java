package io.onedev.server.service;

import io.onedev.server.model.Dashboard;
import io.onedev.server.model.DashboardGroupShare;

import java.util.Collection;

public interface DashboardGroupShareService extends EntityService<DashboardGroupShare> {

	void syncShares(Dashboard dashboard, Collection<String> groupNames);

}
