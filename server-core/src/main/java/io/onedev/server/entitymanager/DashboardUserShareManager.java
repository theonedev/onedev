package io.onedev.server.entitymanager;

import io.onedev.server.model.Dashboard;
import io.onedev.server.model.DashboardUserShare;
import io.onedev.server.persistence.dao.EntityManager;

import java.util.Collection;

public interface DashboardUserShareManager extends EntityManager<DashboardUserShare> {

	void syncShares(Dashboard dashboard, Collection<String> userNames);

}