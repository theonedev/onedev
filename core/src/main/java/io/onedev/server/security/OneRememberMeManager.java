package io.onedev.server.security;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.web.mgt.CookieRememberMeManager;

import io.onedev.server.entitymanager.UserManager;

@Singleton
public class OneRememberMeManager extends CookieRememberMeManager {

	private final UserManager userManager;
	
	@Inject
	public OneRememberMeManager(UserManager userManager) {
		this.userManager = userManager;
	}

	@Override
	protected PrincipalCollection deserialize(byte[] serializedIdentity) {
		PrincipalCollection principals = super.deserialize(serializedIdentity);
		if (principals != null) {
			Long userId = (Long) principals.getPrimaryPrincipal();
			if (userManager.get(userId) != null)
				return principals;
			else
				return null;
		} else {
			return null;
		}
	}
	
}
