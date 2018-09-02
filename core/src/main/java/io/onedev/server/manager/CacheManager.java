package io.onedev.server.manager;

import java.util.Map;

import javax.annotation.Nullable;

import io.onedev.server.util.facade.MembershipFacade;
import io.onedev.server.util.facade.ProjectFacade;
import io.onedev.server.util.facade.TeamFacade;
import io.onedev.server.util.facade.UserFacade;

public interface CacheManager {
	
	Map<Long, ProjectFacade> getProjects();
	
	Map<Long, UserFacade> getUsers();
	
	Map<Long, TeamFacade> getTeams();
	
	Map<Long, MembershipFacade> getMemberships();
	
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
	
	@Nullable
	Long getTeamId(Long projectId, String name);
	
}
