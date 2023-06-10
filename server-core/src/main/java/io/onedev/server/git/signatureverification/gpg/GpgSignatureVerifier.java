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
import org.eclipse.jgit.util.RawParseUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static java.nio.charset.StandardCharsets.UTF_8;

@Singleton
public class GpgSignatureVerifier implements SignatureVerifier {
	
	private static final Logger logger = LoggerFactory.getLogger(GpgSignatureVerifier.class);
	
	private static final byte[] SIGNATURE_START = "-----BEGIN PGP SIGNATURE-----\n".getBytes(UTF_8);
	
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
		GpgSigningKey verificationKey = gpgSetting.findSignatureVerificationKey(keyId);
		if (verificationKey == null)
			verificationKey = gpgKeyManager.findSignatureVerificationKey(keyId);
		return verificationKey;
	}	

	@Nullable
	@Override
	public VerificationResult verify(byte[] data, byte[] signatureData, String emailAddress) {
		if (RawParseUtils.match(signatureData, 0, SIGNATURE_START) != -1) {
			try (InputStream is = new ByteArrayInputStream(signatureData)) {
				PGPSignature signature = parseSignature(is);
				if (signature != null) {
					GpgSigningKey verificationKey = loadKey(signature.getKeyID());
					if (verificationKey != null) {
						if (verificationKey.getEmailAddresses() != null 
								&& !verificationKey.getEmailAddresses().contains(emailAddress)) {
							return new GpgVerificationFailed(verificationKey, "Not a verified email of signing GPG key");
						}
						signature.init(
								new JcaPGPContentVerifierBuilderProvider().setProvider(BouncyCastleProvider.PROVIDER_NAME),
								verificationKey.getPublicKey());
						signature.update(data);
						if (signature.verify()) 
							return new GpgVerificationSuccessful(verificationKey);
						else 
							return new GpgVerificationFailed(verificationKey, "Invalid GPG signature");
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
		} else {
			return null;
		}
	}
	
}
