package io.onedev.server.git.command;

import java.io.File;
import java.io.OutputStream;

import io.onedev.commons.utils.command.Commandline;
import io.onedev.commons.utils.command.ExecutionResult;
import io.onedev.server.git.GitUtils;

public class AdvertiseUploadRefsCommand {

	private final File workingDir;

	private final OutputStream stdout;

	private final OutputStream stderr;

	private String protocol;

	public AdvertiseUploadRefsCommand(File workingDir, OutputStream stdout, OutputStream stderr) {
		this.workingDir = workingDir;
		this.stdout = stdout;
		this.stderr = stderr;
	}

	public AdvertiseUploadRefsCommand protocol(String protocol) {
		this.protocol = protocol;
		return this;
	}

	protected Commandline newGit() {
		return GitUtils.newGit();
	}

	public ExecutionResult run() {
		Commandline git = newGit().workingDir(workingDir);

		if (protocol != null)
			git.envs().put("GIT_PROTOCOL", protocol);

		git.addArgs("upload-pack", "--stateless-rpc", "--advertise-refs", ".");
		return git.execute(stdout, stderr);
	}

}
