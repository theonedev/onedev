package io.onedev.server.entitymanager;

import java.util.Collection;
import java.util.List;

import javax.annotation.Nullable;

import io.onedev.server.model.User;
import io.onedev.server.persistence.dao.EntityManager;

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
	
	void replicate(User user);
	
	void delete(User user);
	
	/**
	 * Find root user in the system. 
	 * 
	 * @return
	 * 			root user of the system. Never be <tt>null</tt>
	 */
	User getRoot();

	/**
	 * Find system user in the system. 
	 * 
	 * @return
	 * 			system user. Never be <tt>null</tt>
	 */
	User getSystem();
	
	/**
	 * Find unknown user in the system. 
	 * 
	 * @return
	 * 			unknown user. Never be <tt>null</tt>
	 */
	User getUnknown();
	
	/**
	 * Find user of specified name.
	 * <p>
	 * @param userName
	 * 			name of the user
	 * @return
	 * 			matching user, or <tt>null</tt> if not found 
	 */
	@Nullable 
	User findByName(String userName);
	
	@Nullable
	User findByFullName(String fullName);

	@Nullable
	User findByAccessToken(String accessToken);
	
	@Nullable
	User findByVerifiedEmailAddress(String emailAddress);
	
	List<User> query(@Nullable String term, int firstResult, int maxResults);
	
	int count(String term);
	
	List<User> queryAndSort(Collection<User> topUsers);
	
	void onRenameSsoConnector(String oldName, String newName);
	 
	void onDeleteSsoConnector(String name);
	
}
