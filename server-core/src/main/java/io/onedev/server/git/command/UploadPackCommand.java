package io.onedev.server.git.command;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import javax.annotation.Nullable;

import io.onedev.commons.utils.command.Commandline;
import io.onedev.commons.utils.command.ExecutionResult;
import io.onedev.server.git.CommandUtils;

public class UploadPackCommand {

	private final File workingDir;
	
	private final Map<String, String> envs;
	
	private final InputStream stdin;
	
	private final OutputStream stdout;
	
	private final OutputStream stderr;
	
	private boolean statelessRpc;
	
	private String protocol;
	
	public UploadPackCommand(File workingDir, InputStream stdin, OutputStream stdout, 
			OutputStream stderr, Map<String, String> envs) {
		this.workingDir = workingDir;
		this.stdin = stdin;
		this.stdout = stdout;
		this.stderr = stderr;
		this.envs = envs;
	}
	
	public UploadPackCommand statelessRpc(boolean statelessRpc) {
		this.statelessRpc = statelessRpc;
		return this;
	}
	
	public UploadPackCommand protocol(@Nullable String protocol) {
		this.protocol = protocol;
		return this;
	}
	
	protected Commandline newGit() {
		return CommandUtils.newGit();
	}
	
	public ExecutionResult run() {
		Commandline git = newGit().workingDir(workingDir);
		git.environments().putAll(envs);
		
		if (protocol != null)
			git.environments().put("GIT_PROTOCOL", protocol);
		
		git.addArgs("upload-pack");
		if (statelessRpc)
			git.addArgs("--stateless-rpc");
		git.addArgs(".");
		
		return git.execute(stdout, stderr, stdin);
	}

}
