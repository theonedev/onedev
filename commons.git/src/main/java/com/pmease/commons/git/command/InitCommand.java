package com.pmease.commons.git.command;

import java.io.File;

import com.pmease.commons.util.execution.Commandline;

public class InitCommand extends GitCommand<Void> {

	private boolean bare;
	
	public InitCommand(File repoDir) {
		super(repoDir);
	}
	
	public InitCommand bare(boolean bare) {
		this.bare = bare;
		return this;
	}

	@Override
	public Void call() {
		Commandline cmd = cmd().addArgs("init");
		
		if (bare)
			cmd.addArgs("--bare");
		
		cmd.execute(debugLogger(), errorLogger()).checkReturnCode();
		
		return null;
	}

}
