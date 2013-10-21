package com.pmease.commons.git;

import com.google.common.base.Preconditions;
import com.pmease.commons.util.execution.Commandline;

public class UpdateRefCommand extends GitCommand<Git> {

    private String refName;
    
    private String revision;
    
    private String oldRevision;
    
    private String reason;
    
	public UpdateRefCommand(Git git) {
		super(git);
	}
	
	public UpdateRefCommand refName(String refName) {
	    this.refName = refName;
		return this;
	}
	
	public UpdateRefCommand revision(String revision) {
	    this.revision = revision;
	    return this;
	}
	
	public UpdateRefCommand oldRevision(String oldRevision) {
	    this.oldRevision = oldRevision;
	    return this;
	}
	
	public UpdateRefCommand reason(String reason) {
	    this.reason = reason;
	    return this;
	}

	@Override
	public Git call() {
	    Preconditions.checkNotNull(refName, "refName has to be specified.");
	    Preconditions.checkNotNull(revision, "revision has to be specified.");
	    
		Commandline cmd = git().cmd().addArgs("update-ref", refName, revision);
		if (oldRevision != null)
		    cmd.addArgs(oldRevision);

		if (reason != null)
            cmd.addArgs(reason);
		
		cmd.execute(debugLogger(), errorLogger()).checkReturnCode();
		
		return git();
	}

}
