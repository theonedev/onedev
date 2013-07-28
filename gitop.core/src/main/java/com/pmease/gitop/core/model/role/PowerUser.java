package com.pmease.gitop.core.model.role;

import org.apache.shiro.authz.Permission;

/**
 * Power users can do anything as system administrator except that they can not 
 * manage other user accounts and can not access repositories not authorized by 
 * the owners.   
 * 
 * @author robin
 *
 */
@SuppressWarnings("serial")
public class PowerUser implements Role {

	@Override
	public boolean implies(Permission permission) {
		return permission instanceof PowerUser 
				|| new NormalUser().implies(permission);
	}

}
