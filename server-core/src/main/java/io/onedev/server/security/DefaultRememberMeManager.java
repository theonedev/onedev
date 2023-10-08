package io.onedev.server.security;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.web.mgt.CookieRememberMeManager;

import io.onedev.server.manager.UserManager;
import io.onedev.server.util.CryptoUtils;

@Singleton
public class DefaultRememberMeManager extends CookieRememberMeManager {

	private final UserManager userManager;
	
	@Inject
	public DefaultRememberMeManager(UserManager userManager) {
		this.userManager = userManager;
	}

	@Override
	public byte[] getEncryptionCipherKey() {
		return CryptoUtils.getCipherKey();
	}

	@Override
	public byte[] getDecryptionCipherKey() {
		return CryptoUtils.getCipherKey();
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
