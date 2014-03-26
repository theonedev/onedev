package com.pmease.commons.git.command;

import java.io.File;

import com.google.common.base.Preconditions;
import com.pmease.commons.util.execution.Commandline;
import com.pmease.commons.util.execution.LineConsumer;

public class PushCommand extends GitCommand<Void> {

    private String to;
    
    private String refspec;
    
	public PushCommand(final File repoDir) {
		super(repoDir);
	}

	public PushCommand to(String to) {
	    this.to = to;
	    return this;
	}
	
	public PushCommand refspec(String refspec) {
		this.refspec = refspec;
		return this;
	}
	
	@Override
	public Void call() {
	    Preconditions.checkNotNull(to, "to param has to be specified.");
	    Preconditions.checkNotNull(refspec, "refspec param has to be specified.");
	    
		Commandline cmd = cmd().addArgs("push");
		cmd.addArgs(to, refspec);
		
		cmd.execute(debugLogger, new LineConsumer() {

			@Override
			public void consume(String line) {
				if (line.startsWith("To "))
					info(line);
				else if (line.startsWith(" * [new branch]"))
					info(line);
				else
					error(line);
			}
			
		}).checkReturnCode();
		
		return null;
	}

}
