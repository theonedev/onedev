package io.onedev.server.ssh;

import java.util.Map;

import javax.annotation.Nullable;

import org.apache.sshd.server.command.Command;

public interface SshCommandCreator {
	
	@Nullable
	Command createCommand(String command, Map<String, String> env);
	
}
