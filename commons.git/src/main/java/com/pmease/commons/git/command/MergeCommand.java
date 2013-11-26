package com.pmease.commons.git.command;

import java.io.File;

import com.google.common.base.Preconditions;
import com.pmease.commons.util.execution.Commandline;

public class MergeCommand extends GitCommand<Void> {

    private String revision;
    
    private String message;
    
	public MergeCommand(final File repoDir) {
		super(repoDir);
	}

	public MergeCommand revision(String revision) {
	    this.revision = revision;
	    return this;
	}
	
	public MergeCommand message(String message) {
		this.message = message;
		return this;
	}
	
	@Override
	public Void call() {
	    Preconditions.checkNotNull(revision, "revision has to be specified.");
	    
		Commandline cmd = cmd().addArgs("merge");
		cmd.addArgs("--no-edit", "--log", "--quiet");
		if (message != null)
			cmd.addArgs("-m", message);
		
		cmd.addArgs(revision);
		
		cmd.execute(debugLogger(), errorLogger()).checkReturnCode();
		
		return null;
	}

}
