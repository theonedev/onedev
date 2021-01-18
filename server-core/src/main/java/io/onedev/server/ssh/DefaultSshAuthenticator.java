package io.onedev.server.ssh;

import java.security.PublicKey;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.sshd.common.AttributeRepository.AttributeKey;
import org.apache.sshd.common.config.keys.KeyUtils;
import org.apache.sshd.server.auth.AsyncAuthException;
import org.apache.sshd.server.session.ServerSession;

import io.onedev.server.entitymanager.SshKeyManager;
import io.onedev.server.model.SshKey;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.security.CipherUtils;

@Singleton
public class DefaultSshAuthenticator implements SshAuthenticator {

    private static final AttributeKey<Long> ATTR_PUBLIC_KEY_OWNER_ID = new AttributeKey<>();

	private final SshKeyManager sshKeyManager;

	@Inject
	public DefaultSshAuthenticator(SshKeyManager sshKeyManager) {
		this.sshKeyManager = sshKeyManager;
	}
	
	@Sessional
	@Override
	public boolean authenticate(String username, PublicKey key, ServerSession session) throws AsyncAuthException {
        String digest = KeyUtils.getFingerPrint(CipherUtils.DIGEST_FORMAT, key);  
        SshKey sshKey = sshKeyManager.findByDigest(digest);
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
