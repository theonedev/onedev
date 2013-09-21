package com.pmease.gitop.core.permission;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.shiro.authc.credential.CredentialsMatcher;

import com.pmease.commons.hibernate.dao.GeneralDao;
import com.pmease.commons.shiro.AbstractRealm;
import com.pmease.commons.shiro.AbstractUser;
import com.pmease.gitop.core.manager.RoleManager;
import com.pmease.gitop.core.manager.TeamManager;
import com.pmease.gitop.core.manager.UserManager;
import com.pmease.gitop.core.model.User;

@Singleton
public class UserRealm extends AbstractRealm {

	private final UserManager userManager;
	
	@Inject
	public UserRealm(GeneralDao generalDao, CredentialsMatcher credentialsMatcher, 
			UserManager userManager, RoleManager roleManager, TeamManager teamManager) {
		super(credentialsMatcher);

		this.userManager = userManager;
	}

	@Override
	protected AbstractUser getUserById(Long userId) {
		if (userId != 0L) {
			return userManager.load(userId);
		} else { 
			User user = new User();
			user.setId(0L);
			return user;
		}
	}

	@Override
	protected AbstractUser getUserByName(String userName) {
		return userManager.find(userName);
	}

}
