package com.pmease.gitop.core.manager;

import java.util.Collection;

import com.google.inject.ImplementedBy;
import com.pmease.commons.persistence.dao.GenericDao;
import com.pmease.commons.util.namedentity.EntityLoader;
import com.pmease.gitop.core.manager.impl.DefaultTeamManager;
import com.pmease.gitop.core.model.Team;
import com.pmease.gitop.core.model.User;

@ImplementedBy(DefaultTeamManager.class)
public interface TeamManager extends GenericDao<Team> {

	Collection<Team> getAnonymousTeams();
	
	Collection<Team> getRegisterTeams();

	/**
	 * Find team of specified name belonging to specified owner.
	 * <p>
	 * @param owner
	 * 			user owns the team
	 * @param teamName
	 * 			name of the team
	 * @return
	 * 			matching team, or <i>null</i> if not found 
	 */
	Team lookupTeam(User owner, String teamName);
	
	EntityLoader asEntityLoader(User owner);
	
}
