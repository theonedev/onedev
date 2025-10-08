package io.onedev.server.ssh;

import java.util.Map;

import org.jspecify.annotations.Nullable;

import org.apache.sshd.server.command.Command;

public interface CommandCreator {
	
	@Nullable
	Command createCommand(String commandString, Map<String, String> environments);
	
}
