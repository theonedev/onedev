package com.pmease.gitop.core.entitymanager;

import com.pmease.commons.persistence.dao.GenericDao;
import com.pmease.gitop.core.model.User;

public interface UserManager extends GenericDao<User> {
	/**
	 * Find root user in the system. 
	 * 
	 * @return
	 * 			root user of the system, or null if root user has not been populated
	 */
	public User findRoot();
}
