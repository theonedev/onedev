package io.onedev.server.manager;

import java.util.Collection;
import java.util.Map;

import javax.annotation.Nullable;

import io.onedev.server.util.facade.ConfigurationFacade;
import io.onedev.server.util.facade.MembershipFacade;
import io.onedev.server.util.facade.ProjectFacade;
import io.onedev.server.util.facade.TeamFacade;
import io.onedev.server.util.facade.UserFacade;

public interface CacheManager {
	
	Map<Long, ProjectFacade> getProjects();
	
	Map<Long, UserFacade> getUsers();
	
	Map<Long, TeamFacade> getTeams();
	
	Map<Long, MembershipFacade> getMemberships();
	
	Map<Long, ConfigurationFacade> getConfigurations();
	
	@Nullable
	ProjectFacade getProject(Long id);
	
	@Nullable
	UserFacade getUser(Long id);
	
	@Nullable
	TeamFacade getTeam(Long id);
	
	@Nullable
	MembershipFacade getMembership(Long id);
	
	@Nullable
	Long getUserIdByName(String name);
	
	@Nullable
	Long getUserIdByEmail(String email);
	
	Map<String, Long> getProjectIds();
	
	@Nullable
	Long getProjectIdByName(String name);
	
	Collection<Long> getIssueNumbers(Long projectId);
	
	Collection<Long> getBuildIdsByProject(Long projectId);
	
	Collection<Long> getBuildIdsByConfiguration(Long configurationId);
	
	Collection<Long> filterBuildIds(Long projectId, Collection<String> commitHashes);
	
}
