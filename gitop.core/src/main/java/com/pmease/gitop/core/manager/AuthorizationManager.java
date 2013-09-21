package com.pmease.gitop.core.manager;

import com.google.inject.ImplementedBy;
import com.pmease.commons.hibernate.dao.GenericDao;
import com.pmease.gitop.core.manager.impl.DefaultAuthorizationManager;
import com.pmease.gitop.core.model.RepositoryAuthorization;

@ImplementedBy(DefaultAuthorizationManager.class)
public interface AuthorizationManager extends GenericDao<RepositoryAuthorization> {
	
}
