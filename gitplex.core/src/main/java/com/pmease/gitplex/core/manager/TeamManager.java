package com.pmease.gitplex.core.manager;

import com.pmease.gitplex.core.entity.Account;

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
	 * Rename specified team in specified organization
	 * 
	 * @param organization
	 * 			organization to rename team in
	 * @param oldTeamName
	 * 			old name of the team which should exist in the organization
	 * @param newTeamName
	 * 			new name of the team
	 */
	void rename(Account organization, String oldTeamName, String newTeamName);
}
