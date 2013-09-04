package com.pmease.gitop.core.manager;

import com.google.inject.ImplementedBy;
import com.pmease.commons.persistence.dao.GenericDao;
import com.pmease.commons.util.namedentity.EntityLoader;
import com.pmease.gitop.core.manager.impl.DefaultUserManager;
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

	/**
	 * Find user of specified name.
	 * <p>
	 * @param userName
	 * 			name of the user
	 * @return
	 * 			matching user, or <i>null</i> if not found 
	 */
	User lookupUser(String userName);
	
	EntityLoader asEntityLoader();
}
