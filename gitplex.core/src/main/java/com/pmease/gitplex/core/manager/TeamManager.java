package com.pmease.gitplex.core.manager;

import javax.annotation.Nullable;

import com.pmease.commons.hibernate.dao.EntityDao;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.entity.Team;

public interface TeamManager extends EntityDao<Team> {

	/**
	 * Find team of specified name belonging to specified owner.
	 * <p>
	 * @param owner
	 * 			user owns the team
	 * @param teamName
	 * 			name of the team
	 * @return
	 * 			matching team, or <tt>null</tt> if not found 
	 */
	@Nullable
	Team findBy(Account owner, String teamName);
	
	@Nullable
	Team findBy(String teamFQN);
	
	Team getAnonymous(Account user);
	
	Team getLoggedIn(Account user);
	
	Team getOwners(Account user);

	void delete(Team team);
	
	void rename(Account teamOwner, String oldName, String newName);
}
