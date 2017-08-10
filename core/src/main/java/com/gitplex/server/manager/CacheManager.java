package com.gitplex.server.manager;

import java.util.Map;

import javax.annotation.Nullable;

import com.gitplex.server.util.facade.GroupAuthorizationFacade;
import com.gitplex.server.util.facade.GroupFacade;
import com.gitplex.server.util.facade.MembershipFacade;
import com.gitplex.server.util.facade.ProjectFacade;
import com.gitplex.server.util.facade.UserAuthorizationFacade;
import com.gitplex.server.util.facade.UserFacade;

public interface CacheManager {
	
	Map<Long, ProjectFacade> getProjects();
	
	Map<Long, UserFacade> getUsers();
	
	Map<Long, GroupFacade> getGroups();
	
	Map<Long, MembershipFacade> getMemberships();
	
	Map<Long, UserAuthorizationFacade> getUserAuthorizations();
	
	Map<Long, GroupAuthorizationFacade> getGroupAuthorizations();
	
	@Nullable
	ProjectFacade getProject(Long id);
	
	@Nullable
	UserFacade getUser(Long id);
	
	@Nullable
	GroupFacade getGroup(Long id);
	
	@Nullable
	MembershipFacade getMembership(Long id);
	
	@Nullable
	UserAuthorizationFacade getUserAuthorization(Long id);
	
	@Nullable
	GroupAuthorizationFacade getGroupAuthorization(Long id);
	
	@Nullable
	Long getUserIdByName(String name);
	
	@Nullable
	Long getUserIdByEmail(String email);
	
	Map<String, Long> getProjectIds();
	
	@Nullable
	Long getProjectIdByName(String name);
	
	@Nullable
	Long getGroupIdByName(String name);
	
}
