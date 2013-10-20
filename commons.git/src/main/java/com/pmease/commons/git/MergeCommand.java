package com.pmease.commons.git;

import com.google.common.base.Preconditions;
import com.pmease.commons.util.execution.Commandline;

public class MergeCommand extends GitCommand<Git> {

    private String revision;
    
	public MergeCommand(final Git git) {
		super(git);
	}

	/**
	 * Specify git revision for checkout. Can also accept branch name.
	 *  
	 * @param revision
	 *         revision to be checked out
	 * @return
	 *         self
	 */
	public MergeCommand revision(String revision) {
	    this.revision = revision;
	    return this;
	}
	
	@Override
	public Git call() {
	    Preconditions.checkNotNull(revision, "revision has to be specified.");
	    
		Commandline cmd = git().cmd().addArgs("merge");
		cmd.addArgs(revision);
		
		cmd.execute(debugLogger(), errorLogger()).checkReturnCode();
		
		return git();
	}

}
