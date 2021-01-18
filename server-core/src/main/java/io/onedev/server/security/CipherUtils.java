package io.onedev.server.security;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.apache.shiro.crypto.AesCipherService;
import org.apache.sshd.common.digest.BaseDigest;
import org.apache.sshd.common.digest.Digest;
import org.apache.sshd.common.digest.DigestUtils;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.SettingManager;

public class CipherUtils {

    public static final Digest DIGEST_FORMAT = new BaseDigest("MD5", 512);
	
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

	public static byte[] encrypt(byte[] data) {
		return cipherService.encrypt(data, getCipherKey()).getBytes();
	}
	
	public static byte[] decrypt(byte[] data) {
		return cipherService.decrypt(data, getCipherKey()).getBytes();
	}
	
	public static String digest(String content) {
		try {
			return DigestUtils.getFingerPrint(DIGEST_FORMAT, content);
		} catch (Exception e) {
			throw new RuntimeException(e);
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
