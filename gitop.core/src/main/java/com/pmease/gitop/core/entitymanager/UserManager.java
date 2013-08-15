package com.pmease.gitop.core.entitymanager;

import com.google.inject.ImplementedBy;
import com.pmease.commons.persistence.dao.GenericDao;
import com.pmease.gitop.core.entitymanager.impl.DefaultUserManager;
import com.pmease.gitop.core.model.User;

@ImplementedBy(DefaultUserManager.class)
public interface UserManager extends GenericDao<User> {
	/**
	 * Find root account in the system. 
	 * 
	 * @return
	 * 			root account of the system, or null if root account has not been populated
	 */
	User getRootUser();
}
