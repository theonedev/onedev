package com.pmease.commons.git.command;

import java.io.File;

import com.google.common.base.Preconditions;
import com.pmease.commons.util.execution.Commandline;

public class MergeCommand extends GitCommand<Void> {

    private String revision;
    
	public MergeCommand(final File repoDir) {
		super(repoDir);
	}

	public MergeCommand revision(String revision) {
	    this.revision = revision;
	    return this;
	}
	
	@Override
	public Void call() {
	    Preconditions.checkNotNull(revision, "revision has to be specified.");
	    
		Commandline cmd = cmd().addArgs("merge");
		cmd.addArgs(revision);
		
		cmd.execute(debugLogger(), errorLogger()).checkReturnCode();
		
		return null;
	}

}
