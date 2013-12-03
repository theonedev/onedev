package com.pmease.gitop.web;

import java.util.Collection;
import java.util.List;

import javax.inject.Singleton;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.pmease.commons.hibernate.Transactional;
import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.model.Membership;
import com.pmease.gitop.model.User;

@Singleton
public class GitopHelper {
	
	@Inject
	GitopHelper() {
	}
	
	@Transactional
	public List<User> getManagableAccounts(User user) {
		Collection<Membership> memberships = user.getMemberships();
		List<User> result = Lists.newArrayList();
		for (Membership each : memberships) {
			if (each.getTeam().isOwners()) {
				result.add(each.getTeam().getOwner());
			}
		}
		
		return result;
	}
	
	public static GitopHelper getInstance() {
		return Gitop.getInstance(GitopHelper.class);
	}
}
