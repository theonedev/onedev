package com.pmease.gitplex.core.manager;

import java.util.List;

import javax.annotation.Nullable;

import org.eclipse.jgit.lib.PersonIdent;

import com.pmease.commons.hibernate.dao.EntityDao;
import com.pmease.gitplex.core.entity.Account;

public interface AccountManager extends EntityDao<Account> {
	
	/**
	 * Save specified account
	 * 
	 * @param account
	 * 			account to save
	 * @param oldName
	 * 			in case of rename, this parameter should hold the original name 
	 * 			when above account object is initially loaded to ensure database
	 * 			integrity. Use <tt>null</tt> if original name does not exist, 
	 * 			or the name is not changed
	 */
	void save(Account account, @Nullable String oldName);
	
	void delete(Account account);
	
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
	 * @param accountName
	 * 			name of the user
	 * @return
	 * 			matching user, or <tt>null</tt> if not found 
	 */
	@Nullable Account findByName(String accountName);
	
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
	
	List<Account> allUsers();
	
	List<Account> allOrganizations();
	
}
