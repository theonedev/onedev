package io.onedev.server.git.command;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.onedev.commons.utils.command.Commandline;
import io.onedev.commons.utils.command.LineConsumer;
import io.onedev.server.git.CommandUtils;

public class FetchCommand {

	private static final Logger logger = LoggerFactory.getLogger(FetchCommand.class);
	
	private final File workingDir;
	
    private final String from;
    
    private boolean force;
    
    private boolean quiet;
    
    private String[] refspec = new String[0];
    
	public FetchCommand(File workingDir, String from) {
		this.workingDir = workingDir;
		this.from = from;
	}
	
	public FetchCommand refspec(String... refspec) {
		this.refspec = refspec;
		return this;
	}
	
	public FetchCommand force(boolean force) {
		this.force = force;
		return this;
	}
	
	public FetchCommand quiet(boolean quiet) {
		this.quiet = quiet;
		return this;
	}
	
	protected Commandline newGit() {
		return CommandUtils.newGit();
	}
	
	public void run() {
		Commandline git = newGit().workingDir(workingDir).addArgs("fetch", from);
		if (force)
			git.addArgs("--force");
		if (quiet)
			git.addArgs("--quiet");
		
		for (String each: refspec)
			git.addArgs(each);
		
		git.execute(new LineConsumer() {

			@Override
			public void consume(String line) {
				logger.trace(line);
			}
			
		}, new LineConsumer() {

			@Override
			public void consume(String line) {
				logger.warn(line);
			}
			
		}).checkReturnCode();
	}

}
