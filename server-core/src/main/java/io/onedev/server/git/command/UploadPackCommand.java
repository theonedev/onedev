package io.onedev.server.git.command;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import com.google.common.base.Preconditions;

import io.onedev.commons.utils.command.Commandline;
import io.onedev.commons.utils.command.ExecutionResult;

public class UploadPackCommand extends GitCommand<ExecutionResult> {

	private InputStream stdin;
	
	private OutputStream stdout;
	
	private OutputStream stderr;
	
	private boolean statelessRpc;
	
	public UploadPackCommand(File gitDir, Map<String, String> environments) {
		super(gitDir, environments);
	}
	
	public UploadPackCommand stdin(InputStream stdin) {
		this.stdin = stdin;
		return this;
	}
	
	public UploadPackCommand stdout(OutputStream stdout) {
		this.stdout = stdout;
		return this;
	}
	
	public UploadPackCommand stderr(OutputStream stderr) {
		this.stderr = stderr;
		return this;
	}

	public UploadPackCommand statelessRpc(boolean statelessRpc) {
		this.statelessRpc = statelessRpc;
		return this;
	}
	
	@Override
	public ExecutionResult call() {
		Preconditions.checkNotNull(stdin);
		Preconditions.checkNotNull(stdout);
		Preconditions.checkNotNull(stderr);
		
		Commandline cmd = cmd();
		cmd.addArgs("upload-pack");
		if (statelessRpc)
			cmd.addArgs("--stateless-rpc");
		cmd.addArgs(".");
		
		return cmd.execute(stdout, stderr, stdin);
	}

}
