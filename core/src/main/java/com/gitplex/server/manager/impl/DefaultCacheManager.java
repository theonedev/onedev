package com.gitplex.server.manager.impl;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Singleton;

import com.gitplex.server.manager.CacheManager;
import com.gitplex.server.model.Membership;
import com.gitplex.server.util.facade.UserFacade;
import com.gitplex.server.util.facade.ProjectFacade;
import com.gitplex.server.util.facade.GroupAuthorizationFacade;
import com.gitplex.server.util.facade.GroupFacade;
import com.gitplex.server.util.facade.MembershipFacade;
import com.gitplex.server.util.facade.UserAuthorizationFacade;

@Singleton
public class DefaultCacheManager implements CacheManager {

	private final Map<Long, UserFacade> users = new HashMap<>();
	
	private final Map<Long, ProjectFacade> projects = new HashMap<>();
	
	private final Map<Long, GroupFacade> groups = new HashMap<>();
	
	private final Map<Long, MembershipFacade> groupMemberships = new HashMap<>();
	
	private final Map<Long, GroupAuthorizationFacade> groupAuthorizations = new HashMap<>(); 
	
	private final Map<Long, UserAuthorizationFacade> userAuthorizations = new HashMap<>();
}
