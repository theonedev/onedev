package io.onedev.server.entitymanager;

import java.util.Collection;
import java.util.List;

import javax.annotation.Nullable;

import io.onedev.server.model.Dashboard;
import io.onedev.server.model.User;
import io.onedev.server.persistence.dao.EntityManager;

public interface DashboardManager extends EntityManager<Dashboard> {
	
	List<Dashboard> queryAccessible(@Nullable User user);
	
	void create(Dashboard dashboard);
	
	void update(Dashboard dashboard);
	
	@Nullable
	Dashboard find(User owner, String name);

	void syncShares(Dashboard dashboard, boolean forEveryone, 
			Collection<String> groupNames, Collection<String> userNames);

}
