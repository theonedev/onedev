package com.pmease.gitop.core.manager;

import com.google.inject.ImplementedBy;
import com.pmease.commons.hibernate.dao.GenericDao;
import com.pmease.gitop.core.manager.impl.DefaultRepositoryAuthorizationByTeamManager;
import com.pmease.gitop.core.model.RepositoryAuthorizationByTeam;

@ImplementedBy(DefaultRepositoryAuthorizationByTeamManager.class)
public interface RepositoryAuthorizationByTeamManager extends GenericDao<RepositoryAuthorizationByTeam> {
	
}
