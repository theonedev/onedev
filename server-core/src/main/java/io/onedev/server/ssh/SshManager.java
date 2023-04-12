package io.onedev.server.ssh;

import org.apache.sshd.client.session.ClientSession;

import java.util.UUID;

public interface SshManager {

	ClientSession ssh(String server);
	
}
