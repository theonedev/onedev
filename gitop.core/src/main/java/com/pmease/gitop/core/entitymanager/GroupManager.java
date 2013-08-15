package com.pmease.gitop.core.entitymanager;

import java.util.Collection;

import com.google.inject.ImplementedBy;
import com.pmease.commons.persistence.dao.GenericDao;
import com.pmease.gitop.core.entitymanager.impl.DefaultGroupManager;
import com.pmease.gitop.core.model.Group;

@ImplementedBy(DefaultGroupManager.class)
public interface GroupManager extends GenericDao<Group> {

	Collection<Group> getGroups(Long userId);
	
}
