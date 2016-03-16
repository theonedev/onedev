package com.pmease.gitplex.core.manager;

import javax.annotation.Nullable;

import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.entity.component.Team;

public interface TeamManager {
	/**
	 * Delete specified team from specified organization
	 * 
	 * @param organization
	 * 			organization to delete team from
	 * @param teamName
	 * 			name of team to delete. Note that this name should exist in the organization
	 */
	void delete(Account organization, String teamName);
	
	/**
	 * Save specified team in specified organization
	 * 
	 * @param organization
	 * 			organization to rename team in
	 * @param team
	 * 			team to save
	 * @param oldTeamName
	 * 			old name of the team which should exist in the organization
	 */
	void save(Account organization, Team team, @Nullable String oldTeamName);
}
