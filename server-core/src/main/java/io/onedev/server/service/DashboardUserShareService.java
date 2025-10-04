package io.onedev.server.service;

import io.onedev.server.model.Dashboard;
import io.onedev.server.model.DashboardUserShare;

import java.util.Collection;

public interface DashboardUserShareService extends EntityService<DashboardUserShare> {

	void syncShares(Dashboard dashboard, Collection<String> userNames);

}