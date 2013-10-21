package com.pmease.commons.git;

import com.google.common.base.Preconditions;
import com.pmease.commons.util.execution.Commandline;

/**
 * This class can be used to create new branches.
 * 
 * @author robin
 *
 */
public class BranchCommand extends GitCommand<Git> {

    private String branchName;
    
	public BranchCommand(final Git git) {
		super(git);
	}

	public BranchCommand branchName(String branchName) {
	    this.branchName = branchName;
	    return this;
	}
	
	@Override
	public Git call() {
	    Preconditions.checkNotNull(branchName, "branch has to be specified.");
	    
		Commandline cmd = git().cmd().addArgs("checkout", "-b");
		cmd.addArgs(branchName);
		
		cmd.execute(debugLogger(), errorLogger()).checkReturnCode();
		
		return git();
	}

}
