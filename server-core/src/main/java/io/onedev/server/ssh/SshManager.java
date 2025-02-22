package io.onedev.server.ssh;

import org.apache.sshd.client.session.ClientSession;

public interface SshManager {

	ClientSession ssh(String server);
	
}
