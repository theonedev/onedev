package com.pmease.commons.git;

import com.google.common.base.Preconditions;
import com.pmease.commons.util.execution.Commandline;

public class CommitCommand extends GitCommand<Git> {

	private String message;
	
	private boolean amend;
	
	public CommitCommand(final Git git) {
		super(git);
	}
	
	public CommitCommand message(final String message) {
		this.message = message;
		return this;
	}
	
	public CommitCommand amend(final boolean amend) {
		this.amend = amend;
		return this;
	}

	@Override
	public Git call() {
		Preconditions.checkNotNull(message, "Commit message has to be specified.");
		
		Commandline cmd = git().cmd().addArgs("commit");
		
		cmd.addArgs("-m", message);
		if (amend) 
			cmd.addArgs("--amend");
		
		cmd.execute(debugLogger(), errorLogger()).checkReturnCode();
		
		return git();
	}

}
