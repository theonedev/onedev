package io.onedev.server.git.signatureverification.ssh;

import com.trilead.ssh2.packets.TypesReader;
import com.trilead.ssh2.packets.TypesWriter;
import io.onedev.server.entitymanager.EmailAddressManager;
import io.onedev.server.entitymanager.SshKeyManager;
import io.onedev.server.git.signatureverification.SignatureVerifier;
import io.onedev.server.git.signatureverification.VerificationResult;
import io.onedev.server.util.ExceptionUtils;
import org.apache.sshd.common.digest.BuiltinDigests;
import org.bouncycastle.crypto.Signer;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.digests.SHA512Digest;
import org.bouncycastle.crypto.signers.Ed25519Signer;
import org.bouncycastle.crypto.signers.RSADigestSigner;
import org.bouncycastle.util.io.pem.PemReader;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.StringReader;

import static org.apache.sshd.common.digest.DigestUtils.getFingerPrint;
import static org.bouncycastle.crypto.util.OpenSSHPublicKeyUtil.parsePublicKey;

@Singleton
public class SshSignatureVerifier implements SignatureVerifier {
	
	private final SshKeyManager sshKeyManager;
	
	private final EmailAddressManager emailAddressManager;
	
	@Inject
	public SshSignatureVerifier(SshKeyManager sshKeyManager, EmailAddressManager emailAddressManager) {
		this.sshKeyManager = sshKeyManager;
		this.emailAddressManager = emailAddressManager;
	}

	@Override
	public VerificationResult verify(byte[] data, byte[] signatureData, String emailAddressValue) {
		try (var pemReader = new PemReader(new StringReader(new String(signatureData)))) {
			var signatureBlob = pemReader.readPemObject().getContent();
			var signatureBlobReader = new TypesReader(signatureBlob);
			var magicPreamble = new String(signatureBlobReader.readBytes(6));
			if (!magicPreamble.equals("SSHSIG"))
				return new SshVerificationFailed(null, "Malformed ssh signature");
			var version = signatureBlobReader.readUINT32();
			if (version != 1)
				return new SshVerificationFailed(null, "Unsupported ssh signature version: " + version);
			var publicKeyBytes = signatureBlobReader.readByteString();
			var keyType = new TypesReader(publicKeyBytes).readString();
			var fingerprint = getFingerPrint(BuiltinDigests.sha256, publicKeyBytes);
			var publicKey = parsePublicKey(publicKeyBytes);
			var keyInfo = new SshKeyInfo(keyType, fingerprint);
			
			var sshKey = sshKeyManager.findByFingerprint(fingerprint);
			if (sshKey == null)
				return new SshVerificationFailed(keyInfo, "Signed with an unknown ssh key");
			
			var emailAddress = emailAddressManager.findByValue(emailAddressValue);
			if (emailAddress == null 
					|| !emailAddress.isVerified() 
					|| !emailAddress.getOwner().equals(sshKey.getOwner())) {
				return new SshVerificationFailed(keyInfo, "Not a verified email of signing ssh key owner");
			}
			
			var namespace = signatureBlobReader.readString();
			if (!namespace.equals("git"))
				return new SshVerificationFailed(keyInfo, "Unexpected ssh signature namespace: " + namespace);
			var reserved = signatureBlobReader.readString();
			
			var hashAlgorithm = signatureBlobReader.readString();
			byte[] hash;
			if (hashAlgorithm.equals("sha256"))  
				hash = org.apache.commons.codec.digest.DigestUtils.sha256(data);
			else if (hashAlgorithm.equals("sha512")) 
				hash = org.apache.commons.codec.digest.DigestUtils.sha512(data);					
			else 
				return new SshVerificationFailed(keyInfo, "Unexpected ssh signature hash algorithm: " + hashAlgorithm);
			
			var writer = new TypesWriter();
			writer.writeBytes(magicPreamble.getBytes());
			writer.writeString(namespace);
			writer.writeString(reserved);
			writer.writeString(hashAlgorithm);
			writer.writeString(hash, 0, hash.length);
			
			var sshSignature = signatureBlobReader.readByteString();
			
			var sshSignatureReader  = new TypesReader(sshSignature);
			var signatureAlgorithm = sshSignatureReader.readString();
			Signer signer;
			switch (signatureAlgorithm) {
				case "rsa-sha2-256":
					signer = new RSADigestSigner(new SHA256Digest());
					break;
				case "rsa-sha2-512":
					signer = new RSADigestSigner(new SHA512Digest());
					break;
				case "ssh-ed25519":
					signer = new Ed25519Signer();
					break;
				default:
					return new SshVerificationFailed(keyInfo, "Unsupported ssh signature algorithm: " + signatureAlgorithm);
			}
			
			signer.init(false, publicKey);
			var signBytes = writer.getBytes();
			signer.update(signBytes, 0, signBytes.length);
			if (signer.verifySignature(sshSignatureReader.readByteString()))
				return new SshVerificationSuccessful(keyInfo);
			else
				return new SshVerificationFailed(keyInfo, "Invalid ssh signature");
		} catch (Exception e) {
			throw ExceptionUtils.unchecked(e);
		}
	}

	@Override
	public String getPrefix() {
		return "-----BEGIN SSH SIGNATURE-----";
	}

}
