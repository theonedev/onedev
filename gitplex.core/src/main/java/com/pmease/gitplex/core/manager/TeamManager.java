package com.pmease.gitplex.core.manager;

import javax.annotation.Nullable;

import com.pmease.gitplex.core.model.Team;
import com.pmease.gitplex.core.model.User;

public interface TeamManager {

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
	Team findBy(User owner, String teamName);
	
	@Nullable
	Team findBy(String teamFQN);
	
	Team getAnonymous(User user);
	
	Team getLoggedIn(User user);
	
	Team getOwners(User user);

}
