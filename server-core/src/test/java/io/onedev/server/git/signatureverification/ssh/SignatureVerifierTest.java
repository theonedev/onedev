package io.onedev.server.git.signatureverification.ssh;

import com.google.common.collect.Sets;
import com.google.common.io.Resources;
import io.onedev.commons.utils.FileUtils;
import io.onedev.server.entitymanager.EmailAddressManager;
import io.onedev.server.entitymanager.GpgKeyManager;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.entitymanager.SshKeyManager;
import io.onedev.server.git.signatureverification.DefaultSignatureVerificationManager;
import io.onedev.server.git.signatureverification.VerificationSuccessful;
import io.onedev.server.git.signatureverification.gpg.GpgSignatureVerifier;
import io.onedev.server.git.signatureverification.gpg.GpgSigningKey;
import io.onedev.server.model.EmailAddress;
import io.onedev.server.model.SshKey;
import io.onedev.server.model.User;
import io.onedev.server.model.support.administration.GpgSetting;
import io.onedev.server.util.GpgUtils;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevObject;
import org.eclipse.jgit.revwalk.RevWalk;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SignatureVerifierTest {

    @Test
    public void verify() {
		var tempDir = FileUtils.createTempDir();
		try (InputStream is = Resources.getResource(SignatureVerifierTest.class, "git-signature.zip").openStream()) {
			FileUtils.unzip(is, tempDir);
			try (Git git = Git.open(tempDir)) {
				var emailAddressValue = "foo@example.com";
				var owner = new User();
				owner.setId(1L);
				var emailAddress = new EmailAddress();
				emailAddress.setValue(emailAddressValue);
				emailAddress.setOwner(owner);
				emailAddress.setVerificationCode(null);
				
				var emailAddressManager = mock(EmailAddressManager.class);
				when(emailAddressManager.findByValue(any())).thenReturn(emailAddress);
				
				var sshKeyManager = mock(SshKeyManager.class);
				var sshKey = new SshKey();
				sshKey.setOwner(owner);
				when(sshKeyManager.findByFingerprint(any())).thenReturn(sshKey);
				
				var gpgKeyManager = mock(GpgKeyManager.class);
				when(gpgKeyManager.findSigningKey(anyLong())).thenReturn(new GpgSigningKey() {
					@Override
					public PGPPublicKey getPublicKey() {
						var armoredKey = "" +
								"-----BEGIN PGP PUBLIC KEY BLOCK-----\n" +
								"\n" +
								"mDMEZIUyvxYJKwYBBAHaRw8BAQdA72vvNIlCWWDvN4a/RRtg7+GkxZ2DFNry7zpn\n" +
								"Xbj/vAe0FUZvbyA8Zm9vQGV4YW1wbGUuY29tPoiZBBMWCgBBFiEE0KAUvnJD8mPD\n" +
								"ePn8LNDZisNCH84FAmSFMr8CGwMFCQPCZwAFCwkIBwICIgIGFQoJCAsCBBYCAwEC\n" +
								"HgcCF4AACgkQLNDZisNCH84nXQD+L35Lun8E68mJWfPReURT8RaMIO5n0/CIbj2q\n" +
								"9Mz3mN4BAOd0ygKOuCAVw3jFQGlBRchHHzLW86cifCDhskS4mwEDuDgEZIUyvxIK\n" +
								"KwYBBAGXVQEFAQEHQGCDpBqVzCSAgi6wQkbXsVWobl0DRkeIoFobHbbttkERAwEI\n" +
								"B4h+BBgWCgAmFiEE0KAUvnJD8mPDePn8LNDZisNCH84FAmSFMr8CGwwFCQPCZwAA\n" +
								"CgkQLNDZisNCH87VQAD/aRrNMjjW2wq52B2Ed3IBdbJNqLur9hNDZotnebjFwI4A\n" +
								"/i0iZZTUO3X3n45RtntAknKA0MBLcmYsn2rOzshQMcUO\n" +
								"=m9mT\n" +
								"-----END PGP PUBLIC KEY BLOCK-----";
						return GpgUtils.parse(armoredKey).iterator().next();
					}

					@Override
					public Collection<String> getEmailAddresses() {
						return Sets.newHashSet(emailAddressValue);
					}
				});
				
				var gpgSetting = mock(GpgSetting.class);
				when(gpgSetting.findSigningKey(anyLong())).thenReturn(null);
				
				var settingManager = mock(SettingManager.class);
				when(settingManager.getGpgSetting()).thenReturn(gpgSetting);

				var signatureVerifiers = Sets.newHashSet(
						new SshSignatureVerifier(sshKeyManager, emailAddressManager),
						new GpgSignatureVerifier(gpgKeyManager, settingManager)
				);
				var commitSignatureManager = new DefaultSignatureVerificationManager(signatureVerifiers);
				
				RevObject revObject = git.getRepository().parseCommit(ObjectId.fromString("2c968a0c073b0d1887aae917abea3a56629b3e0a"));
				assert(commitSignatureManager.verifySignature(revObject) instanceof VerificationSuccessful);
				
				revObject = git.getRepository().parseCommit(ObjectId.fromString("adb9fa57a4a139d2326075424322a866a7c7b115"));
				assert(commitSignatureManager.verifySignature(revObject) instanceof VerificationSuccessful);
				
				revObject = git.getRepository().parseCommit(ObjectId.fromString("cd0af2811fc837f68d001b06cbce1e1101f73942"));
				assert(commitSignatureManager.verifySignature(revObject) instanceof VerificationSuccessful);
			
				try (var revWalk = new RevWalk(git.getRepository())) {
					revObject = revWalk.parseTag(git.getRepository().resolve("v1"));
					assert(commitSignatureManager.verifySignature(revObject) instanceof VerificationSuccessful);

					revObject = revWalk.parseTag(git.getRepository().resolve("v2"));
					assert(commitSignatureManager.verifySignature(revObject) instanceof VerificationSuccessful);

					revObject = revWalk.parseTag(git.getRepository().resolve("v3"));
					assert(commitSignatureManager.verifySignature(revObject) instanceof VerificationSuccessful);
				}
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			FileUtils.deleteDir(tempDir, 3);
		}		
    }
	
}