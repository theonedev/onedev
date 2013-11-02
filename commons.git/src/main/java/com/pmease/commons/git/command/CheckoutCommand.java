package com.pmease.commons.git.command;

import java.io.File;

import com.google.common.base.Preconditions;
import com.pmease.commons.util.execution.Commandline;

public class CheckoutCommand extends GitCommand<Void> {

    private String revision;
    
    private boolean newBranch;
    
	public CheckoutCommand(final File repoDir) {
		super(repoDir);
	}

	/**
	 * Specify git revision for checkout. Can also accept branch name.
	 *  
	 * @param revision
	 *         revision to be checked out
	 * @return
	 *         self
	 */
	public CheckoutCommand revision(String revision) {
	    this.revision = revision;
	    return this;
	}
	
	public CheckoutCommand newBranch(boolean newBranch) {
		this.newBranch = newBranch;
		return this;
	}
	
	@Override
	public Void call() {
	    Preconditions.checkNotNull(revision, "revision has to be specified.");
	    
		Commandline cmd = cmd().addArgs("checkout");
		if (newBranch)
			cmd.addArgs("-b");
		
		cmd.addArgs(revision);
		
		cmd.execute(debugLogger(), errorLogger()).checkReturnCode();
		
		return null;
	}

}
