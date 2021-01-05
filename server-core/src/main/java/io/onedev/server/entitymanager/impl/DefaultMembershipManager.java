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

import io.onedev.server.entitymanager.GroupManager;
import io.onedev.server.entitymanager.MembershipManager;
import io.onedev.server.model.Group;
import io.onedev.server.model.Membership;
import io.onedev.server.model.User;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.BaseEntityManager;
import io.onedev.server.persistence.dao.Dao;

@Singleton
public class DefaultMembershipManager extends BaseEntityManager<Membership> implements MembershipManager {

	private static final Logger logger = LoggerFactory.getLogger(DefaultMembershipManager.class);
	
	private final GroupManager groupManager;
	
	@Inject
	public DefaultMembershipManager(Dao dao, GroupManager groupManager) {
		super(dao);
		this.groupManager = groupManager;
	}

	@Transactional
	@Override
	public void delete(Collection<Membership> memberships) {
		for (Membership membership: memberships)
			dao.remove(membership);
	}

	@Override
	public List<Membership> query() {
		return query(true);
	}
	
	@Override
	public int count() {
		return count(true);
	}

	@Override
	public void syncMemberships(User user, Collection<String> groupNames) {
    	Map<String, Membership> syncMap = new HashMap<>();
    	for (String groupName: groupNames) {
    		Group group = groupManager.find(groupName);
    		if (group == null) {
    			logger.warn("Unable to find group: " + groupName);
    		} else {
    			Membership membership = new Membership();
    			membership.setGroup(group);
    			membership.setUser(user);
    			syncMap.put(groupName, membership);
    		}
    	}

    	Map<String, Membership> currentMap = new HashMap<>();
		user.getMemberships().forEach(membership -> 
				currentMap.put(membership.getGroup().getName(), membership));
		
		MapDifference<String, Membership> diff = Maps.difference(currentMap, syncMap);
		
		diff.entriesOnlyOnLeft().values().forEach(membership -> delete(membership));
		diff.entriesOnlyOnRight().values().forEach(membership -> save(membership));		
	}

}
