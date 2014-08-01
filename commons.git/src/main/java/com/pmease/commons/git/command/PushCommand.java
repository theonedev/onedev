package com.pmease.commons.git.command;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.pmease.commons.util.execution.Commandline;
import com.pmease.commons.util.execution.LineConsumer;

public class PushCommand extends GitCommand<Void> {

	private static final Logger logger = LoggerFactory.getLogger(PushCommand.class);
	
    private String to;
    
    private String refspec[] = new String[0];
    
	public PushCommand(final File repoDir) {
		super(repoDir);
	}

	public PushCommand to(String to) {
	    this.to = to;
	    return this;
	}
	
	public PushCommand refspec(String... refspec) {
		this.refspec = refspec;
		return this;
	}
	
	@Override
	public Void call() {
	    Preconditions.checkNotNull(to, "to param has to be specified.");
	    Preconditions.checkNotNull(refspec, "refspec param has to be specified.");
	    
		Commandline cmd = cmd().addArgs("push", to);
		
		for (String each: refspec)
			cmd.addArgs(each);
		
		cmd.execute(new LineConsumer() {

			@Override
			public void consume(String line) {
				logger.debug(line);
			}
			
		}, new LineConsumer() {

			@Override
			public void consume(String line) {
				if (line.startsWith("To "))
					logger.info(line);
				else if (line.startsWith(" * [new branch]"))
					logger.info(line);
				else
					logger.error(line);
			}
			
		}).checkReturnCode();
		
		return null;
	}

}
