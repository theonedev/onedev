package com.gitplex.server.manager;

import javax.annotation.Nullable;

import org.eclipse.jgit.lib.PersonIdent;

import com.gitplex.server.model.User;
import com.gitplex.server.persistence.dao.EntityManager;

public interface UserManager extends EntityManager<User> {
	
	/**
	 * Save specified user
	 * 
	 * @param user
	 * 			user to save
	 * @param oldName
	 * 			in case of rename, this parameter should hold the original name 
	 * 			when above user object is initially loaded to ensure database
	 * 			integrity. Use <tt>null</tt> if original name does not exist, 
	 * 			or the name is not changed
	 */
	void save(User user, @Nullable String oldName);
	
	void delete(User user);
	
	/**
	 * Find root user in the system. 
	 * 
	 * @return
	 * 			root user of the system. Never be <tt>null</tt>
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
	 * Find user of specified name.
	 * <p>
	 * @param userName
	 * 			name of the user
	 * @return
	 * 			matching user, or <tt>null</tt> if not found 
	 */
	@Nullable User findByEmail(String email);
	
	/**
	 * Find user by person
	 * <p>
	 * @param person
	 * 			Git person representation 
	 * @return
	 * 			matching user, or <tt>null</tt> if not found 
	 */
	@Nullable User find(PersonIdent person);
	
	/**
	 * Get current authenticated user in Shiro context, or <tt>null</tt> if not 
	 * authenticated. 
	 * 
	 * @return 
	 *         current authenticated user, or <tt>null</tt> for anonymous access
	 */
	@Nullable User getCurrent();
	
}
