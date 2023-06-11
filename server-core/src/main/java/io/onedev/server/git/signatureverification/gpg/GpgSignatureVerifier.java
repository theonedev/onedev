package io.onedev.server.git.signatureverification.gpg;

import io.onedev.server.entitymanager.GpgKeyManager;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.git.signatureverification.SignatureVerifier;
import io.onedev.server.git.signatureverification.VerificationResult;
import io.onedev.server.model.support.administration.GpgSetting;
import io.onedev.server.util.GpgUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openpgp.*;
import org.bouncycastle.openpgp.jcajce.JcaPGPObjectFactory;
import org.bouncycastle.openpgp.operator.jcajce.JcaPGPContentVerifierBuilderProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

@Singleton
public class GpgSignatureVerifier implements SignatureVerifier {
	
	private static final Logger logger = LoggerFactory.getLogger(GpgSignatureVerifier.class);
	
	private final GpgKeyManager gpgKeyManager;

	private final SettingManager settingManager;
	
	@Inject
	public GpgSignatureVerifier(GpgKeyManager gpgKeyManager, SettingManager settingManager) {
		this.gpgKeyManager = gpgKeyManager;
		this.settingManager = settingManager;
	}

	private PGPSignature parseSignature(InputStream in) throws IOException, PGPException {
		try (InputStream sigIn = PGPUtil.getDecoderStream(in)) {
			JcaPGPObjectFactory pgpFactory = new JcaPGPObjectFactory(sigIn);
			Object obj = pgpFactory.nextObject();
			if (obj instanceof PGPCompressedData) {
				obj = new JcaPGPObjectFactory(((PGPCompressedData) obj).getDataStream()).nextObject();
			}
			if (obj instanceof PGPSignatureList) {
				return ((PGPSignatureList) obj).get(0);
			}
			return null;
		}
	}
	
	private GpgSigningKey loadKey(long keyId) {
		GpgSetting gpgSetting = settingManager.getGpgSetting();
		GpgSigningKey verificationKey = gpgSetting.findSigningKey(keyId);
		if (verificationKey == null)
			verificationKey = gpgKeyManager.findSigningKey(keyId);
		return verificationKey;
	}	

	@Override
	public VerificationResult verify(byte[] data, byte[] signatureData, String emailAddress) {
		try (InputStream is = new ByteArrayInputStream(signatureData)) {
			PGPSignature signature = parseSignature(is);
			if (signature != null) {
				GpgSigningKey signingKey = loadKey(signature.getKeyID());
				if (signingKey != null) {
					if (signingKey.getEmailAddresses() != null 
							&& !signingKey.getEmailAddresses().contains(emailAddress)) {
						return new GpgVerificationFailed(signingKey, "Not a verified email of signing GPG key");
					}
					signature.init(
							new JcaPGPContentVerifierBuilderProvider().setProvider(BouncyCastleProvider.PROVIDER_NAME),
							signingKey.getPublicKey());
					signature.update(data);
					if (signature.verify()) 
						return new GpgVerificationSuccessful(signingKey);
					else 
						return new GpgVerificationFailed(signingKey, "Invalid GPG signature");
				} else {
					return new GpgVerificationFailed(null, "Signed with an unknown GPG key "
							+ "(key ID: " + GpgUtils.getKeyIDString(signature.getKeyID()) + ")");
				}
			} else {
				return new GpgVerificationFailed(null, "Looks like a GPG signature but without necessary data");
			}
		} catch (PGPException e) {
			logger.error("Error verifying GPG signature", e);
			return new GpgVerificationFailed(null, "Error verifying GPG signature");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String getPrefix() {
		return "-----BEGIN PGP SIGNATURE-----";
	}

}
