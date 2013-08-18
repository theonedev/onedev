package com.pmease.commons.security;

import org.apache.shiro.SecurityUtils;

import com.google.common.base.Preconditions;
import com.pmease.commons.loader.AppLoader;
import com.pmease.commons.persistence.dao.GeneralDao;

public class SecurityHelper extends SecurityUtils {
	
	/**
	 * Get current user.
	 * 
	 * @param userClass
	 * 			Class of the user
	 * @return
	 * 			Current user object or <tt>null</tt> for anonymous access 
	 */
	public static <T extends AbstractUser> T getUser(Class<T> userClass) {
		Object principal = getSubject().getPrincipal();
		Preconditions.checkNotNull(principal);
		Long userId = (Long) principal;
		if (userId != 0L)
			return (T) AppLoader.getInstance(GeneralDao.class).load(userClass, userId);
		else
			return null;
	}
	
}
