package io.onedev.server.service;

import io.onedev.server.model.Dashboard;
import io.onedev.server.model.User;

import org.jspecify.annotations.Nullable;
import java.util.Collection;
import java.util.List;

public interface DashboardService extends EntityService<Dashboard> {
	
	List<Dashboard> queryAccessible(@Nullable User user);
	
	void createOrUpdate(Dashboard dashboard);
	
	@Nullable
	Dashboard find(User owner, String name);

	void syncShares(Dashboard dashboard, boolean forEveryone, 
			Collection<String> groupNames, Collection<String> userNames);

}
