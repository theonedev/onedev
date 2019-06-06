package io.onedev.server.cache;

import java.util.Collection;
import java.util.Map;

import javax.annotation.Nullable;

import io.onedev.server.util.facade.GroupAuthorizationFacade;
import io.onedev.server.util.facade.GroupFacade;
import io.onedev.server.util.facade.MembershipFacade;
import io.onedev.server.util.facade.ProjectFacade;
import io.onedev.server.util.facade.UserAuthorizationFacade;
import io.onedev.server.util.facade.UserFacade;

public interface CacheManager {
	
	Map<Long, ProjectFacade> getProjects();
	
	Map<Long, UserFacade> getUsers();
	
	Map<Long, GroupFacade> getGroups();
	
	Map<Long, UserAuthorizationFacade> getUserAuthorizations();
	
	Map<Long, GroupAuthorizationFacade> getGroupAuthorizations();
	
	Map<Long, MembershipFacade> getMemberships();
	
	@Nullable
	ProjectFacade getProject(Long id);
	
	@Nullable
	UserFacade getUser(Long id);
	
	@Nullable
	GroupFacade getGroup(Long id);
	
	@Nullable
	MembershipFacade getMembership(Long id);
	
	@Nullable
	Long getUserIdByName(String name);
	
	@Nullable
	Long getUserIdByEmail(String email);
	
	@Nullable
	Long getGroupIdByName(String name);
	
	Map<String, Long> getProjectIds();
	
	@Nullable
	Long getProjectIdByName(String name);
	
	Collection<Long> getIssueNumbers(Long projectId);
	
	Collection<Long> getBuildIdsByProject(Long projectId);
	
	Collection<Long> filterBuildIds(Long projectId, Collection<String> commitHashes);
	
	Collection<String> getBuildParamNames();
	
	Collection<String> getBuildParamValues(String paramName);
	
}
