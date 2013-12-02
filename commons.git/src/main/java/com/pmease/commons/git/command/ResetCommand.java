package com.pmease.commons.git.command;

import java.io.File;

import com.pmease.commons.util.execution.Commandline;

public class ResetCommand extends GitCommand<Void> {

	private String commit;
	
	private String mode;
	
	public ResetCommand(File repoDir) {
		super(repoDir);
	}

	public ResetCommand commit(String commit) {
		this.commit = commit;
		return this;
	}
	
	public ResetCommand mode(String mode) {
		this.mode = mode;
		return this;
	}
	
	@Override
	public Void call() {
		Commandline cmd = cmd().addArgs("reset");
		cmd.addArgs("--quiet");
		if (mode != null)
			cmd.addArgs("mode");
		if (commit != null)
			cmd.addArgs(commit);
		
		cmd.execute(debugLogger, errorLogger).checkReturnCode();
		
		return null;
	}

}
