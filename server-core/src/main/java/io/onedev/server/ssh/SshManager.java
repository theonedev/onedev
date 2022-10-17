package io.onedev.server.ssh;

import java.util.UUID;

import org.apache.sshd.client.session.ClientSession;

public interface SshManager {

	ClientSession ssh(UUID serverUUID);
	
}
