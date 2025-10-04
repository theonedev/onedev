package io.onedev.server.ssh;

import org.apache.sshd.client.session.ClientSession;

public interface SshService {

	ClientSession ssh(String server);
	
}
