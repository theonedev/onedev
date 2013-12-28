package com.pmease.commons.git.command;

import java.io.File;

import com.google.common.base.Preconditions;
import com.pmease.commons.util.execution.Commandline;

public class UpdateSymbolicRefCommand extends GitCommand<Void> {

    private String symbolicRefName;

    private String refName;
    
    private String reason;
    
	public UpdateSymbolicRefCommand(File repoDir) {
		super(repoDir);
	}
	
	public UpdateSymbolicRefCommand symbolicRefName(String symbolicRefName) {
	    this.symbolicRefName = symbolicRefName;
		return this;
	}
	
	public UpdateSymbolicRefCommand refName(String refName) {
	    this.refName = refName;
	    return this;
	}
	
	public UpdateSymbolicRefCommand reason(String reason) {
	    this.reason = reason;
	    return this;
	}

	@Override
	public Void call() {
	    Preconditions.checkNotNull(symbolicRefName, "symbolicRefName has to be specified.");
	    Preconditions.checkNotNull(refName, "refName has to be specified.");
	    
		Commandline cmd = cmd().addArgs("symbolic-ref", symbolicRefName, refName);

		if (reason != null)
            cmd.addArgs("-m", reason);
		
		cmd.execute(debugLogger, errorLogger).checkReturnCode();
		
		return null;
	}

}
