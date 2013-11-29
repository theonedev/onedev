package com.pmease.gitop.web;

import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import com.google.common.collect.Lists;
import com.google.inject.Singleton;
import com.pmease.gitop.core.model.Membership;
import com.pmease.gitop.core.model.User;

@Singleton
public class GitopFacade {
	
	@Inject
	GitopFacade() {
	}
	
	public List<User> getManagableUsers(User user) {
		Collection<Membership> memberships = user.getMemberships();
		List<User> result = Lists.newArrayList();
		for (Membership each : memberships) {
			if (each.getTeam().isOwners()) {
				result.add(each.getTeam().getOwner());
			}
		}
		
		return result;
	}
}
