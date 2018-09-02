package io.onedev.server.manager;

import javax.annotation.Nullable;

import io.onedev.server.model.Project;
import io.onedev.server.model.Team;
import io.onedev.server.persistence.dao.EntityManager;

public interface TeamManager extends EntityManager<Team> {
	/**
	 * Save specified team
	 * 
	 * @param team
	 * 			team to save
	 * @param oldName
	 * 			in case of rename, this parameter should hold the original name 
	 * 			when above team object is initially loaded to ensure database 
	 * 			integrity. Use <tt>null</tt> if original name does not exist, 
	 * 			or the name is not changed
	 */
	void save(Team team, @Nullable String oldName);
	
	@Nullable
	Team find(Project project, String name);
	
	@Nullable
	Team find(String projectName, String teamName);
	
	@Nullable
	Team findByFQN(String teamFQN);
	
}
