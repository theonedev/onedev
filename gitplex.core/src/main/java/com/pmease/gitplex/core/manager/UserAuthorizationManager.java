package com.pmease.gitplex.core.manager;

import java.util.Collection;

import javax.annotation.Nullable;

import com.pmease.commons.hibernate.dao.EntityDao;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.entity.Depot;
import com.pmease.gitplex.core.entity.UserAuthorization;

public interface UserAuthorizationManager extends EntityDao<UserAuthorization> {
	
	@Nullable
	UserAuthorization find(Account user, Depot depot);
	
	Collection<UserAuthorization> query(Account organization);
	
}