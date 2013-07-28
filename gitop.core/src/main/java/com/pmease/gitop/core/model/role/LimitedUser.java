package com.pmease.gitop.core.model.role;

import org.apache.shiro.authz.Permission;


/**
 * Limited users can only browse the system without any other permissions. 
 * <p>
 * They are not permitted to create repositories and teams. 
 * Since they can not create repositories, they will not be able to fork existing 
 * repositories even if those repositories are allowed to be forked. 
 *  
 * @author robin
 *
 */
@SuppressWarnings("serial")
public class LimitedUser implements Role {

	@Override
	public boolean implies(Permission permission) {
		return permission instanceof LimitedUser;
	}

}
