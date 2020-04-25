package io.onedev.server.ssh;

import org.apache.sshd.server.auth.pubkey.PublickeyAuthenticator;
import org.apache.sshd.server.session.ServerSession;

public interface SshAuthenticator extends PublickeyAuthenticator {

	Long getPublicKeyOwnerId(ServerSession session);

}
