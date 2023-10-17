package io.onedev.server.util;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.SettingManager;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.shiro.crypto.AesCipherService;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

public class CryptoUtils {
	
	private static final int DEFAULT_SECRET_LEN = 40;
	
	private static volatile KeyPair keyPair;
	
    private static AesCipherService cipherService = new AesCipherService();
    
	public static byte[] getCipherKey() {
		byte[] privateKey = OneDev.getInstance(SettingManager.class).getSshSetting().getPrivateKey().getEncoded();
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
	
	public static String generateSecret(int count) {
		return RandomStringUtils.random(count, 0, 0, true, true, null, 
				new SecureRandom());		
	}

	public static String generateSecret() {
		return generateSecret(DEFAULT_SECRET_LEN);
	}
	
	public static byte[] encrypt(byte[] data) {
		return cipherService.encrypt(data, getCipherKey()).getBytes();
	}
	
	public static byte[] decrypt(byte[] data) {
		return cipherService.decrypt(data, getCipherKey()).getBytes();
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
