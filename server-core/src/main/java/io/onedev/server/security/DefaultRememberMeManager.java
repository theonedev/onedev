package io.onedev.server.security;

import io.onedev.server.crypto.CryptoService;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.web.mgt.CookieRememberMeManager;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class DefaultRememberMeManager extends CookieRememberMeManager {

	@Inject
	private CryptoService cryptoService;

	@Override
	public byte[] getEncryptionCipherKey() {
		return cryptoService.getCipherKey();
	}

	@Override
	public byte[] getDecryptionCipherKey() {
		return cryptoService.getCipherKey();
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
