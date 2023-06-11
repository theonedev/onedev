package io.onedev.server.model.support.administration;

import io.onedev.server.git.signatureverification.gpg.GpgSigningKey;
import io.onedev.server.util.GpgUtils;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPSecretKeyRing;
import org.bouncycastle.openpgp.bc.BcPGPSecretKeyRing;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;

public class GpgSetting implements Serializable {

	private static final long serialVersionUID = 1L;

	private byte[] encodedSigningKey;
	
	private transient PGPSecretKeyRing signingKey;
	
	private Map<Long, String> encodedTrustedKeys = new LinkedHashMap<>();
	
	private transient Map<Long, GpgSigningKey> trustedSignatureVerificationKeys;
	
	private transient Map<Long, List<PGPPublicKey>> trustedKeys;
	
	public GpgSetting() {
		// Add GitHub public GPG key
		encodedTrustedKeys.put(GpgUtils.getKeyID("4AEE18F83AFDEB23"), ""
				+ "-----BEGIN PGP PUBLIC KEY BLOCK-----\n"
				+ "\n"
				+ "xsBNBFmUaEEBCACzXTDt6ZnyaVtueZASBzgnAmK13q9Urgch+sKYeIhdymjuMQta\n"
				+ "x15OklctmrZtqre5kwPUosG3/B2/ikuPYElcHgGPL4uL5Em6S5C/oozfkYzhwRrT\n"
				+ "SQzvYjsE4I34To4UdE9KA97wrQjGoz2Bx72WDLyWwctD3DKQtYeHXswXXtXwKfjQ\n"
				+ "7Fy4+Bf5IPh76dA8NJ6UtjjLIDlKqdxLW4atHe6xWFaJ+XdLUtsAroZcXBeWDCPa\n"
				+ "buXCDscJcLJRKZVc62gOZXXtPfoHqvUPp3nuLA4YjH9bphbrMWMf810Wxz9JTd3v\n"
				+ "yWgGqNY0zbBqeZoGv+TuExlRHT8ASGFS9SVDABEBAAHNNUdpdEh1YiAod2ViLWZs\n"
				+ "b3cgY29tbWl0IHNpZ25pbmcpIDxub3JlcGx5QGdpdGh1Yi5jb20+wsBiBBMBCAAW\n"
				+ "BQJZlGhBCRBK7hj4Ov3rIwIbAwIZAQAAmQEIACATWFmi2oxlBh3wAsySNCNV4IPf\n"
				+ "DDMeh6j80WT7cgoX7V7xqJOxrfrqPEthQ3hgHIm7b5MPQlUr2q+UPL22t/I+ESF6\n"
				+ "9b0QWLFSMJbMSk+BXkvSjH9q8jAO0986/pShPV5DU2sMxnx4LfLfHNhTzjXKokws\n"
				+ "+8ptJ8uhMNIDXfXuzkZHIxoXk3rNcjDN5c5X+sK8UBRH092BIJWCOfaQt7v7wig5\n"
				+ "4Ra28pM9GbHKXVNxmdLpCFyzvyMuCmINYYADsC848QQFFwnd4EQnupo6QvhEVx1O\n"
				+ "j7wDwvuH5dCrLuLwtwXaQh0onG4583p0LGms2Mf5F+Ick6o/4peOlBoZz48=\n"
				+ "=HXDP\n"
				+ "-----END PGP PUBLIC KEY BLOCK-----\n");
	}

	@Nullable
	public byte[] getEncodedSigningKey() {
		return encodedSigningKey;
	}

	public void setEncodedSigningKey(@Nullable byte[] encodedSigningKey) {
		this.encodedSigningKey = encodedSigningKey;
	}
	
	@Nullable
	public PGPSecretKeyRing getSigningKey() {
		if (signingKey == null && encodedSigningKey != null) {
	    	try {
				signingKey = new BcPGPSecretKeyRing(encodedSigningKey);
			} catch (IOException | PGPException e) {
				throw new RuntimeException(e);
			}
		}
		return signingKey;
	}

	public Map<Long, String> getEncodedTrustedKeys() {
		return encodedTrustedKeys;
	}
	
	public void encodedTrustedKeysUpdated() {
		trustedKeys = null;
		trustedSignatureVerificationKeys = null;
	}
	
	public Map<Long, List<PGPPublicKey>> getTrustedKeys() {
		if (trustedKeys == null) {
			trustedKeys = new HashMap<>();
			for (Map.Entry<Long, String> entry: encodedTrustedKeys.entrySet()) 
				trustedKeys.put(entry.getKey(), GpgUtils.parse(entry.getValue()));
		}
		return trustedKeys;
	}

	@Nullable
	public GpgSigningKey getTrustedSignatureVerificationKey(long keyId) {
		return getTrustedSignatureVerificationKeys().get(keyId);
	}
	
	private Map<Long, GpgSigningKey> getTrustedSignatureVerificationKeys() {
		if (trustedSignatureVerificationKeys == null) {
			trustedSignatureVerificationKeys = new HashMap<>();
			for (List<PGPPublicKey> publicKeys: getTrustedKeys().values()) {
				for (PGPPublicKey publicKey: publicKeys) {
					trustedSignatureVerificationKeys.put(publicKey.getKeyID(), new GpgSigningKey() {
						
						@Override
						public PGPPublicKey getPublicKey() {
							return publicKey;
						}
						
						@Override
						public Collection<String> getEmailAddresses() {
							return null;
						}
						
					});
				}
			}
		}
		return trustedSignatureVerificationKeys;
	}
	
	@Nullable
	public GpgSigningKey findSigningKey(long keyId) {
		if (getSigningKey() != null && getSigningKey().getPublicKey().getKeyID() == keyId) {
			return new GpgSigningKey() {
				
				@Override
				public PGPPublicKey getPublicKey() {
					return getSigningKey().getPublicKey();
				}

				@Override
				public Collection<String> getEmailAddresses() {
					return null;
				}
				
			};
		} else {
			return getTrustedSignatureVerificationKey(keyId);
		}
	}
	
}
