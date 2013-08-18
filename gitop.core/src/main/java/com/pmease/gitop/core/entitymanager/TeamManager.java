package com.pmease.gitop.core.entitymanager;

import java.util.Collection;

import com.google.inject.ImplementedBy;
import com.pmease.commons.persistence.dao.GenericDao;
import com.pmease.commons.util.namedentity.EntityLoader;
import com.pmease.gitop.core.entitymanager.impl.DefaultTeamManager;
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
	 * @param name
	 * 			name of the team
	 * @return
	 * 			matching team, or <i>null</i> if not found 
	 */
	Team find(User owner, String name);
	
	EntityLoader asEntityLoader(User owner);
	
}
