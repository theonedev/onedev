package com.pmease.gitop.core.manager;

import com.google.inject.ImplementedBy;
import com.pmease.commons.hibernate.dao.GenericDao;
import com.pmease.gitop.core.manager.impl.DefaultRepositoryAuthorizationByTeamManager;
import com.pmease.gitop.core.model.RepositoryAuthorizationByIndividual;

@ImplementedBy(DefaultRepositoryAuthorizationByTeamManager.class)
public interface RepositoryAuthorizationByIndividualManager extends GenericDao<RepositoryAuthorizationByIndividual> {
	
}
