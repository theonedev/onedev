package io.onedev.server.security;

import io.onedev.server.util.CryptoUtils;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.web.mgt.CookieRememberMeManager;

import javax.inject.Singleton;

@Singleton
public class DefaultRememberMeManager extends CookieRememberMeManager {

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
		if (principals != null && principals.getPrimaryPrincipal() instanceof String) {
			if (SecurityUtils.getAuthUser((String) principals.getPrimaryPrincipal()) != null)
				return principals;
			else
				return null;
		} else {
			return null;
		}
	}

}
