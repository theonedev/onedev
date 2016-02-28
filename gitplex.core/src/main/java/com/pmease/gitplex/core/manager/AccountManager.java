package com.pmease.gitplex.core.manager;

import javax.annotation.Nullable;

import org.eclipse.jgit.lib.PersonIdent;

import com.pmease.commons.hibernate.dao.EntityDao;
import com.pmease.gitplex.core.entity.Account;

public interface AccountManager extends EntityDao<Account> {
	
	/**
	 * Save user properties. Note that name of the user should not be changed 
	 * via this method. Call the {@link this#rename(Long, String, String)} to 
	 * rename an user.
	 */
	void save(Account user);
	
	void delete(Account user);
	
	/**
	 * Find root account in the system. 
	 * 
	 * @return
	 * 			root account of the system. Never be <tt>null</tt>
	 */
	Account getRoot();

	/**
	 * Find user of specified name.
	 * <p>
	 * @param userName
	 * 			name of the user
	 * @return
	 * 			matching user, or <tt>null</tt> if not found 
	 */
	@Nullable Account findByName(String userName);
	
	/**
	 * Find user by person
	 * <p>
	 * @param person
	 * 			Git person representation 
	 * @return
	 * 			matching user, or <tt>null</tt> if not found 
	 */
	@Nullable Account findByPerson(PersonIdent person);
	
	/**
	 * Get current authenticated user in Shiro context, or <tt>null</tt> if not 
	 * authenticated. 
	 * 
	 * @return 
	 *         current authenticated user, or <tt>null</tt> for anonymous access
	 */
	@Nullable Account getCurrent();
	
	/**
	 * Get previous user in case current user is a run-as user
	 * 
	 * @return
	 * 			previous user if current user is a run-as user, or <tt>null</tt> 
	 * 			if current user is not a run-as user. 
	 */
	@Nullable Account getPrevious();
	
	void rename(Long userId, String oldName, String newName);
}
