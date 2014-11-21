package com.pmease.gitplex.core.manager;

import java.util.Collection;

import com.pmease.gitplex.core.model.Team;
import com.pmease.gitplex.core.model.User;
import com.pmease.gitplex.core.permission.operation.GeneralOperation;

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
	Team findBy(User owner, String teamName);
	
	Team getAnonymous(User user);
	
	Team getLoggedIn(User user);
	
	Team getOwners(User user);

	void trim(Collection<Long> teamIds);

	GeneralOperation getActualAuthorizedOperation(Team team);
}
