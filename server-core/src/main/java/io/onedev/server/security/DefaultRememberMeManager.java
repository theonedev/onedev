package io.onedev.server.security;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.apache.commons.codec.binary.Base64;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.web.mgt.CookieRememberMeManager;

import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.entitymanager.UserManager;

@Singleton
public class DefaultRememberMeManager extends CookieRememberMeManager {

	private final UserManager userManager;
	
	private final Provider<SettingManager> settingManagerProvider;
	
	private volatile KeyPair keyPair;
	
	@Inject
	public DefaultRememberMeManager(UserManager userManager, Provider<SettingManager> settingManagerProvider) {
		this.userManager = userManager;
		this.settingManagerProvider = settingManagerProvider;
	}

	private byte[] calcCipherKey() {
		byte[] privateKey = settingManagerProvider.get().getSshSetting().getPrivateKey().getEncoded();
		if (keyPair == null || !keyPair.getPrivateKey().equals(privateKey)) {
			/*
			 * We do not use salt here to make sure that the cipher key remains the same after server restart. 
			 * This will not sacrificing security as the private key is already a strong cipher
			 */
			KeySpec spec = new PBEKeySpec(Base64.encodeBase64String(privateKey).toCharArray(), new byte[] {0}, 256, 256);
			try {
				SecretKeyFactory f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
				keyPair = new KeyPair(privateKey, f.generateSecret(spec).getEncoded());
			} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
				throw new RuntimeException(e);
			}		
		} 
		return keyPair.getCipherKey();
	}

	@Override
	public byte[] getEncryptionCipherKey() {
		return calcCipherKey();
	}

	@Override
	public byte[] getDecryptionCipherKey() {
		return calcCipherKey();
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
	
	private static class KeyPair {
		
		private final byte[] privateKey;
		
		private final byte[] cipherKey;
		
		public KeyPair(byte[] privateKey, byte[] cipherKey) {
			this.privateKey = privateKey;
			this.cipherKey = cipherKey;
		}

		public byte[] getPrivateKey() {
			return privateKey;
		}

		public byte[] getCipherKey() {
			return cipherKey;
		}
		
	}
}
