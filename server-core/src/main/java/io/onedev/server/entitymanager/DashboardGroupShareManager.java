package io.onedev.server.entitymanager;

import io.onedev.server.model.Dashboard;
import io.onedev.server.model.DashboardGroupShare;
import io.onedev.server.persistence.dao.EntityManager;

import java.util.Collection;

public interface DashboardGroupShareManager extends EntityManager<DashboardGroupShare> {

	void syncShares(Dashboard dashboard, Collection<String> groupNames);

}
