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

import io.onedev.server.entitymanager.DashboardGroupShareManager;
import io.onedev.server.entitymanager.GroupManager;
import io.onedev.server.model.Dashboard;
import io.onedev.server.model.DashboardGroupShare;
import io.onedev.server.model.Group;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.BaseEntityManager;
import io.onedev.server.persistence.dao.Dao;

@Singleton
public class DefaultDashboardGroupShareManager extends BaseEntityManager<DashboardGroupShare> 
		implements DashboardGroupShareManager {
	
	private static final Logger logger = LoggerFactory.getLogger(DefaultDashboardGroupShareManager.class);
	
	private final GroupManager groupManager;

	@Inject
	public DefaultDashboardGroupShareManager(Dao dao, GroupManager groupManager) {
		super(dao);
		this.groupManager = groupManager;
	}

	@Transactional
	@Override
	public void syncShares(Dashboard dashboard, Collection<String> groupNames) {
    	Map<String, DashboardGroupShare> syncMap = new HashMap<>();
    	for (String groupName: groupNames) {
    		Group group = groupManager.find(groupName);
    		if (group == null) {
    			logger.warn("Unable to find group: " + groupName);
    		} else {
    			DashboardGroupShare share = new DashboardGroupShare();
    			share.setGroup(group);
    			share.setDashboard(dashboard);
    			syncMap.put(groupName, share);
    		}
    	}

    	Map<String, DashboardGroupShare> currentMap = new HashMap<>();
		dashboard.getGroupShares().forEach(share -> 
				currentMap.put(share.getGroup().getName(), share));
		
		MapDifference<String, DashboardGroupShare> diff = Maps.difference(currentMap, syncMap);
		
		diff.entriesOnlyOnLeft().values().forEach(share -> delete(share));
		diff.entriesOnlyOnRight().values().forEach(share -> dao.persist(share));		
	}

	@Override
	public List<DashboardGroupShare> query() {
		return query(true);
	}

	@Override
	public int count() {
		return count(true);
	}
	
}
