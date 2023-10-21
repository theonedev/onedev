package io.onedev.server.entitymanager;

import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.persistence.dao.EntityManager;
import io.onedev.server.security.permission.BasePermission;
import io.onedev.server.util.facade.UserCache;
import io.onedev.server.util.facade.UserFacade;

import javax.annotation.Nullable;
import javax.validation.constraints.Null;
import java.util.Collection;
import java.util.List;

public interface UserManager extends EntityManager<User> {
	
	void create(User user);
	
	/**
	 * Update specified user
	 * 
	 * @param user
	 * 			user to save
	 * @param oldName
	 * 			in case of rename, this parameter should hold the original name 
	 * 			when above user object is initially loaded to ensure database
	 * 			integrity. Use <tt>null</tt> if original name does not exist, 
	 * 			or the name is not changed
	 */
	void update(User user, @Nullable String oldName);
	
	void replicate(User user);
	
	void delete(User user);
	
	void delete(Collection<User> users);
	
	void useInternalAuthentication(Collection<User> users);
	
	void useExternalAuthentication(Collection<User> users);

	void setAsGuest(Collection<User> users, boolean guest);
	
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
	UserFacade findFacadeById(Long userId);
	
	@Nullable
	User findByFullName(String fullName);

	@Nullable
	User findByAccessToken(String accessTokenValue);
	
	@Nullable
	User findByPasswordResetCode(String passwordResetCode);

	String createTemporalAccessToken(Long userId, long secondsToExpire);
	
	@Nullable
	User findByVerifiedEmailAddress(String emailAddress);
	
	List<User> query(@Nullable String term, int firstResult, int maxResults);
	
	int count(String term);
	
	int countNonGuests();
	
	void onRenameSsoConnector(String oldName, String newName);
	 
	void onDeleteSsoConnector(String name);

	Collection<User> getAuthorizedUsers(Project project, BasePermission permission);
	
	UserCache cloneCache();
	
}
