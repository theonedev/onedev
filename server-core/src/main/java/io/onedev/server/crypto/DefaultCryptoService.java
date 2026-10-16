package io.onedev.server.crypto;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import io.onedev.server.persistence.annotation.Transactional;
import org.apache.commons.codec.binary.Base64;
import org.apache.shiro.crypto.cipher.AesCipherService;

import io.onedev.commons.loader.ManagedSerializedForm;
import io.onedev.server.cluster.ClusterService;
import io.onedev.server.event.Listen;
import io.onedev.server.event.entity.EntityPersisted;
import io.onedev.server.model.Setting;
import io.onedev.server.persistence.TransactionService;
import io.onedev.server.service.SettingService;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class DefaultCryptoService implements CryptoService, Serializable {

	@Inject
	private SettingService settingService;

	@Inject
	private TransactionService transactionService;

	@Inject
	private ClusterService clusterService;

	private final AesCipherService cipherService = new AesCipherService();

	private volatile byte[] cipherKey;

	@Override
	public byte[] getCipherKey() {
		byte[] key = cipherKey;
		if (key == null) {
			synchronized (this) {
				key = cipherKey;
				if (key == null) {
					byte[] privateKey = settingService.getSshSetting().getPrivateKey().getEncoded();
					// Keep the derivation stable across restarts and compatible with existing ciphertext.
					KeySpec spec = new PBEKeySpec(Base64.encodeBase64String(privateKey).toCharArray(), new byte[] {0}, 256, 256);
					try {
						SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
						key = factory.generateSecret(spec).getEncoded();
						cipherKey = key;
					} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
						throw new RuntimeException(e);
					}
				}
			}
		}
		return key;
	}

	@Override
	public byte[] encrypt(byte[] data) {
		return cipherService.encrypt(data, getCipherKey()).getBytes();
	}

	@Override
	public byte[] decrypt(byte[] data) {
		return cipherService.decrypt(data, getCipherKey()).getClonedBytes();
	}

	@Transactional
	@Listen
	public void on(EntityPersisted event) {
		if (event.getEntity() instanceof Setting && ((Setting) event.getEntity()).getKey() == Setting.Key.SSH) {
			transactionService.runAfterCommit(() -> clusterService.submitToAllServers(() -> {
				synchronized (DefaultCryptoService.this) {
					cipherKey = null;
				}
				return null;
			}));
		}
	}

	public Object writeReplace() throws ObjectStreamException {
		return new ManagedSerializedForm(CryptoService.class);
	}

}
