package com.pmease.gitop.core.manager;

import java.util.Collection;

import com.google.inject.ImplementedBy;
import com.pmease.commons.hibernate.dao.GenericDao;
import com.pmease.gitop.core.manager.impl.DefaultAuthorizationManager;
import com.pmease.gitop.model.Authorization;
import com.pmease.gitop.model.Repository;
import com.pmease.gitop.model.PullRequest;
import com.pmease.gitop.model.User;
import com.pmease.gitop.model.Vote;
import com.pmease.gitop.model.permission.operation.GeneralOperation;

@ImplementedBy(DefaultAuthorizationManager.class)
public interface AuthorizationManager extends GenericDao<Authorization> {
	
	Collection<User> listAuthorizedUsers(Repository repository, GeneralOperation operation);
	
	boolean canModify(PullRequest request);
	
	boolean canModify(Vote vote);
	
}
