package com.pmease.commons.git.command;

import java.io.File;

import com.google.common.base.Preconditions;
import com.pmease.commons.util.execution.Commandline;

public class CloneCommand extends GitCommand<Void> {

	private String from;
	
	private boolean bare;
	
	public CloneCommand(File repoDir) {
		super(repoDir);
	}

	public CloneCommand from(String from) {
		this.from = from;
		return this;
	}
	
	public CloneCommand bare(boolean bare) {
		this.bare = bare;
		return this;
	}
	
	@Override
	public Void call() {
		Preconditions.checkNotNull(from, "from has to be specified.");
		
		Commandline cmd = cmd().addArgs("clone");
		if (bare)
			cmd.addArgs("--bare");
		cmd.addArgs(from);
		cmd.addArgs(".");
		
		cmd.execute(debugLogger(), errorLogger()).checkReturnCode();
		
		return null;
	}

}
