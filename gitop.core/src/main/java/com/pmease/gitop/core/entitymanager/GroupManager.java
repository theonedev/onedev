package com.pmease.gitop.core.entitymanager;

import java.util.Collection;

import com.google.inject.ImplementedBy;
import com.pmease.commons.persistence.dao.GenericDao;
import com.pmease.gitop.core.entitymanager.impl.DefaultGroupManager;
import com.pmease.gitop.core.model.Team;

@ImplementedBy(DefaultGroupManager.class)
public interface GroupManager extends GenericDao<Team> {

	Collection<Team> getGroups(Long userId);
	
}
