package com.pmease.gitop.core.entitymanager;

import java.util.Collection;

import com.google.inject.ImplementedBy;
import com.pmease.commons.persistence.dao.GenericDao;
import com.pmease.gitop.core.entitymanager.impl.DefaultTeamManager;
import com.pmease.gitop.core.model.Team;

@ImplementedBy(DefaultTeamManager.class)
public interface TeamManager extends GenericDao<Team> {

	Collection<Team> getAnonymousTeams();
	
	Collection<Team> getRegisterTeams();

}
