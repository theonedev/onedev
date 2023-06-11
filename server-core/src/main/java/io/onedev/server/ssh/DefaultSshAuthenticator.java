package io.onedev.server.ssh;

import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.entitymanager.SshKeyManager;
import io.onedev.server.model.SshKey;
import io.onedev.server.model.User;
import io.onedev.server.persistence.annotation.Sessional;
import org.apache.sshd.common.AttributeRepository.AttributeKey;
import org.apache.sshd.common.config.keys.KeyUtils;
import org.apache.sshd.common.digest.BuiltinDigests;
import org.apache.sshd.server.auth.AsyncAuthException;
import org.apache.sshd.server.session.ServerSession;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.PublicKey;

@Singleton
public class DefaultSshAuthenticator implements SshAuthenticator {

    private static final AttributeKey<Long> ATTR_PUBLIC_KEY_OWNER_ID = new AttributeKey<>();

	private final SshKeyManager sshKeyManager;
	
	private final SettingManager settingManager;

	@Inject
	public DefaultSshAuthenticator(SshKeyManager sshKeyManager, SettingManager settingManager) {
		this.sshKeyManager = sshKeyManager;
		this.settingManager = settingManager;
	}
	
	@Sessional
	@Override
	public boolean authenticate(String username, PublicKey key, ServerSession session) 
			throws AsyncAuthException {
		try {
			PrivateKey privateKey = settingManager.getSshSetting().getPrivateKey();
			if (key.equals(KeyUtils.recoverPublicKey(privateKey))) {
				session.setAttribute(ATTR_PUBLIC_KEY_OWNER_ID, User.SYSTEM_ID);
				return true;
			}
		} catch (GeneralSecurityException e) {
			throw new RuntimeException(e);
		}
		
        String digest = KeyUtils.getFingerPrint(BuiltinDigests.sha256, key);  
        SshKey sshKey = sshKeyManager.findByFingerprint(digest);
        if (sshKey != null) {
            session.setAttribute(ATTR_PUBLIC_KEY_OWNER_ID, sshKey.getOwner().getId());
            return true;
        } else {
        	return false;
        }
	}

	@Override
	public Long getPublicKeyOwnerId(ServerSession session) {
		return session.getAttribute(ATTR_PUBLIC_KEY_OWNER_ID);
	}

}
