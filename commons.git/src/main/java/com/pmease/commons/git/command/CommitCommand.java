package com.pmease.commons.git.command;

import java.io.File;

import com.google.common.base.Preconditions;
import com.pmease.commons.util.execution.Commandline;

public class CommitCommand extends GitCommand<Void> {

	private String message;
	
	private boolean amend;
	
	private boolean add;
	
	public CommitCommand(final File repoDir) {
		super(repoDir);
	}
	
	public CommitCommand message(final String message) {
		this.message = message;
		return this;
	}
	
	public CommitCommand amend(final boolean amend) {
		this.amend = amend;
		return this;
	}
	
	public CommitCommand add(final boolean add) {
		this.add = add;
		return this;
	}

	@Override
	public Void call() {
		Preconditions.checkNotNull(message, "Commit message has to be specified.");
		
		Commandline cmd = cmd().addArgs("commit");
		
		cmd.addArgs("-m", message);
		if (add)
			cmd.addArgs("-a");
		if (amend) 
			cmd.addArgs("--amend");
		
		cmd.execute(debugLogger, errorLogger).checkReturnCode();
		
		return null;
	}

}
