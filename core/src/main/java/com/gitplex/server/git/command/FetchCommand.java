package com.gitplex.server.git.command;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gitplex.utils.command.Commandline;
import com.gitplex.utils.command.LineConsumer;
import com.google.common.base.Preconditions;

public class FetchCommand extends GitCommand<Void> {

	private static final Logger logger = LoggerFactory.getLogger(FetchCommand.class);
	
    private String from;
    
    private String[] refspec = new String[0];
    
	public FetchCommand(final File gitDir) {
		super(gitDir);
	}

	public FetchCommand from(String from) {
	    this.from = from;
	    return this;
	}
	
	public FetchCommand refspec(String... refspec) {
		this.refspec = refspec;
		return this;
	}
	
	@Override
	public Void call() {
	    Preconditions.checkNotNull(from, "from param has to be specified.");
	    
		Commandline cmd = cmd().addArgs("fetch");
		cmd.addArgs(from);
		cmd.addArgs("--force");
		
		for (String each: refspec)
			cmd.addArgs(each);
		
		cmd.execute(new LineConsumer() {

			@Override
			public void consume(String line) {
				logger.trace(line);
			}
			
		}, new LineConsumer() {

			@Override
			public void consume(String line) {
				if (line.startsWith("From ") 
						|| line.startsWith(" * branch") 
						|| line.startsWith(" * [new ref]") 
						|| line.contains("..") && line.contains("->")) {
					logger.info(line);
				} else {
					logger.error(line);
				}
			}
			
		}).checkReturnCode();
		
		return null;
	}

}
