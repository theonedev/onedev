package com.pmease.commons.git;

import com.pmease.commons.util.execution.Commandline;

public class InitCommand extends GitCommand<Git> {

	private boolean bare;
	
	public InitCommand(Git git) {
		super(git);
	}
	
	public InitCommand bare(boolean bare) {
		this.bare = bare;
		return this;
	}

	@Override
	public Git call() {
		Commandline cmd = git().cmd().addArgs("init");
		
		if (bare)
			cmd.addArgs("--bare");
		
		cmd.execute(debugLogger(), errorLogger()).checkReturnCode();
		
		return git();
	}

}
