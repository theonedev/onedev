package io.onedev.server.entitymanager.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;

import io.onedev.server.entitymanager.DashboardUserShareManager;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.model.Dashboard;
import io.onedev.server.model.DashboardUserShare;
import io.onedev.server.model.User;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.BaseEntityManager;
import io.onedev.server.persistence.dao.Dao;

@Singleton
public class DefaultDashboardUserShareManager extends BaseEntityManager<DashboardUserShare> 
		implements DashboardUserShareManager {
	
	private static final Logger logger = LoggerFactory.getLogger(DefaultDashboardUserShareManager.class);
	
	private final UserManager userManager;

	@Inject
	public DefaultDashboardUserShareManager(Dao dao, UserManager userManager) {
		super(dao);
		this.userManager = userManager;
	}
	
	@Override
	public List<DashboardUserShare> query() {
		return query(true);
	}

	@Override
	public int count() {
		return count(true);
	}
	
	@Transactional
	@Override
	public void syncShares(Dashboard dashboard, Collection<String> userNames) {
    	Map<String, DashboardUserShare> syncMap = new HashMap<>();
    	for (String userName: userNames) {
    		User user = userManager.findByName(userName);
    		if (user == null) {
    			logger.warn("Unable to find user: " + userName);
    		} else {
    			DashboardUserShare share = new DashboardUserShare();
    			share.setUser(user);
    			share.setDashboard(dashboard);
    			syncMap.put(userName, share);
    		}
    	}

    	Map<String, DashboardUserShare> currentMap = new HashMap<>();
		dashboard.getUserShares().forEach(share -> 
				currentMap.put(share.getUser().getName(), share));
		
		MapDifference<String, DashboardUserShare> diff = Maps.difference(currentMap, syncMap);
		
		diff.entriesOnlyOnLeft().values().forEach(share -> delete(share));
		diff.entriesOnlyOnRight().values().forEach(share -> dao.persist(share));		
	}

}
