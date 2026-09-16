package io.onedev.server.security;

import io.onedev.server.util.CryptoUtils;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.web.mgt.CookieRememberMeManager;

import jakarta.inject.Singleton;

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
	protected RememberedIdentity deserialize(byte[] serializedIdentity) {
		RememberedIdentity identity = super.deserialize(serializedIdentity);
		PrincipalCollection principals = identity != null ? identity.principals() : null;
		if (principals != null && principals.getPrimaryPrincipal() instanceof String) {
			if (SecurityUtils.getAuthUser((String) principals.getPrimaryPrincipal()) != null)
				return identity;
			else
				return null;
		} else {
			return null;
		}
	}

}
