package com.pmease.commons.git.command;

import java.io.File;

import com.google.common.base.Preconditions;
import com.pmease.commons.util.execution.Commandline;
import com.pmease.commons.util.execution.LineConsumer;

public class CheckoutCommand extends GitCommand<Void> {

    private String revision;
    
    private String newBranch;
    
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
	
	public CheckoutCommand newBranch(String newBranch) {
		this.newBranch = newBranch;
		return this;
	}
	
	@Override
	public Void call() {
	    Preconditions.checkNotNull(revision, "revision has to be specified.");
	    
		Commandline cmd = cmd().addArgs("checkout");
		if (newBranch != null)
			cmd.addArgs("-b", newBranch);
		
		cmd.addArgs(revision);
		
		cmd.execute(debugLogger(), new LineConsumer() {

			@Override
			public void consume(String line) {
				if (line.startsWith("Switched to a new branch"))
					info(line);
				else
					error(line);
			}
			
		}).checkReturnCode();
		
		return null;
	}

}
