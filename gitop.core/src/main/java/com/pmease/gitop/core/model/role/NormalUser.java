package com.pmease.gitop.core.model.role;

import org.apache.shiro.authz.Permission;

/**
 * Normal users have all permissions of restricted users, and can also create their 
 * own repositories as well as being able to create teams. 
 * <p>
 * They will be able to fork existing repositories if those repositories are allowed 
 * to be forked. 
 * 
 * @author robin
 *
 */
@SuppressWarnings("serial")
public class NormalUser implements Role {

	@Override
	public boolean implies(Permission permission) {
		return permission instanceof NormalUser 
				|| new LimitedUser().implies(permission);
	}

}
