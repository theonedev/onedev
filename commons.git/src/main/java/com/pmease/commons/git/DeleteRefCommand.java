package com.pmease.commons.git;

import com.google.common.base.Preconditions;
import com.pmease.commons.util.execution.Commandline;

public class DeleteRefCommand extends GitCommand<Git> {

    private String refName;
    
    private String oldRevision;
    
    private String reason;
    
	public DeleteRefCommand(Git git) {
		super(git);
	}
	
	public DeleteRefCommand refName(String refName) {
	    this.refName = refName;
		return this;
	}
	
	public DeleteRefCommand oldRevision(String oldRevision) {
	    this.oldRevision = oldRevision;
	    return this;
	}
	
	public DeleteRefCommand reason(String reason) {
	    this.reason = reason;
	    return this;
	}

	@Override
	public Git call() {
        Preconditions.checkNotNull(refName, "refName has to be specified.");

        Commandline cmd = git().cmd().addArgs("update-ref", "-d", refName);
		if (oldRevision != null)
		    cmd.addArgs(oldRevision);
		
		if (reason != null)
		    cmd.addArgs(reason);
		
		cmd.execute(debugLogger(), errorLogger()).checkReturnCode();
		
		return git();
	}

}
