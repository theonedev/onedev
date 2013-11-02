package com.pmease.commons.git.command;

import java.io.File;

import com.google.common.base.Preconditions;
import com.pmease.commons.util.execution.Commandline;

public class UpdateRefCommand extends GitCommand<Void> {

    private String refName;
    
    private String revision;
    
    private String oldRevision;
    
    private String reason;
    
	public UpdateRefCommand(File repoDir) {
		super(repoDir);
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
	public Void call() {
	    Preconditions.checkNotNull(refName, "refName has to be specified.");
	    Preconditions.checkNotNull(revision, "revision has to be specified.");
	    
		Commandline cmd = cmd().addArgs("update-ref", refName, revision);
		if (oldRevision != null)
		    cmd.addArgs(oldRevision);

		if (reason != null)
            cmd.addArgs(reason);
		
		cmd.execute(debugLogger(), errorLogger()).checkReturnCode();
		
		return null;
	}

}
