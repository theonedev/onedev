package io.onedev.server.ssh;

import java.util.Map;

import javax.annotation.Nullable;

import org.apache.sshd.server.command.Command;

public interface CommandCreator {
	
	@Nullable
	Command createCommand(String commandString, Map<String, String> environments);
	
}
