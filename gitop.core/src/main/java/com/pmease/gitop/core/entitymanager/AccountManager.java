package com.pmease.gitop.core.entitymanager;

import com.google.inject.ImplementedBy;
import com.pmease.commons.persistence.dao.GenericDao;
import com.pmease.gitop.core.entitymanager.impl.DefaultAccountManager;
import com.pmease.gitop.core.model.Account;

@ImplementedBy(DefaultAccountManager.class)
public interface AccountManager extends GenericDao<Account> {
	/**
	 * Find root account in the system. 
	 * 
	 * @return
	 * 			root account of the system, or null if root account has not been populated
	 */
	Account getRootAccount();
}
