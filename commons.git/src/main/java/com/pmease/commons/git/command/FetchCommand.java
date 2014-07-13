package com.pmease.commons.git.command;

import java.io.File;

import com.google.common.base.Preconditions;
import com.pmease.commons.util.execution.Commandline;
import com.pmease.commons.util.execution.LineConsumer;

public class FetchCommand extends GitCommand<Void> {

    private String from;
    
    private String refspec;
    
	public FetchCommand(final File repoDir) {
		super(repoDir);
	}

	public FetchCommand from(String from) {
	    this.from = from;
	    return this;
	}
	
	public FetchCommand refspec(String refspec) {
		this.refspec = refspec;
		return this;
	}
	
	@Override
	public Void call() {
	    Preconditions.checkNotNull(from, "from param has to be specified.");
	    
		Commandline cmd = cmd().addArgs("fetch");
		cmd.addArgs(from);
		
		if (refspec != null)
			cmd.addArgs(refspec);
		
		cmd.execute(debugLogger, new LineConsumer() {

			@Override
			public void consume(String line) {
				if (line.startsWith("From ") || line.startsWith(" * branch"))
					info(line);
				else if (line.startsWith(" * [new ref]"))
					info(line);
				else
					error(line);
			}
			
		}).checkReturnCode();
		
		return null;
	}

}
