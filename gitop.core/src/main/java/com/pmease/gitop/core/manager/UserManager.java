package com.pmease.gitop.core.manager;

import java.util.Collection;
import java.util.List;

import javax.annotation.Nullable;

import com.google.inject.ImplementedBy;
import com.pmease.commons.hibernate.dao.GenericDao;
import com.pmease.gitop.core.manager.impl.DefaultUserManager;
import com.pmease.gitop.model.User;

@ImplementedBy(DefaultUserManager.class)
public interface UserManager extends GenericDao<User> {
	/**
	 * Find root account in the system. 
	 * 
	 * @return
	 * 			root account of the system. Never be <tt>null</tt>
	 */
	User getRoot();

	/**
	 * Find user of specified name.
	 * <p>
	 * @param userName
	 * 			name of the user
	 * @return
	 * 			matching user, or <tt>null</tt> if not found 
	 */
	@Nullable User findByName(String userName);
	
	/**
	 * Find user of specified email.
	 * <p>
	 * @param email
	 * 			email of the user
	 * @return
	 * 			matching user, or <tt>null</tt> if not found 
	 */
	@Nullable User findByEmail(String email);
	
	/**
	 * Get current authenticated user in Shiro context, or <tt>null</tt> if not 
	 * authenticated. 
	 * 
	 * @return 
	 *         current authenticated user, or <tt>null</tt> for anonymous access
	 */
	@Nullable User getCurrent();
	
	void trim(Collection<Long> userIds);
	
	/**
	 * Get all accounts that the specified user can manage/admin
	 * 
	 * @param user
	 * @return all accounts can be managed
	 */
	List<User> getManagableAccounts(User user);
	
}
