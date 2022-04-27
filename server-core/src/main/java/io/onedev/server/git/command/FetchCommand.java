package io.onedev.server.git.command;

import java.io.File;
import java.util.Map;

import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import io.onedev.commons.utils.command.Commandline;
import io.onedev.commons.utils.command.LineConsumer;

public class FetchCommand extends GitCommand<Void> {

	private static final Logger logger = LoggerFactory.getLogger(FetchCommand.class);
	
    private String from;
    
    private boolean force;
    
    private boolean quiet;
    
    private String[] refspec = new String[0];
    
	public FetchCommand(File gitDir, @Nullable Map<String, String> environments) {
		super(gitDir, environments);
	}

	public FetchCommand from(String from) {
	    this.from = from;
	    return this;
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
	
	@Override
	public Void call() {
	    Preconditions.checkNotNull(from, "from param has to be specified.");
	    
		Commandline cmd = cmd().addArgs("fetch");
		cmd.addArgs(from);
		if (force)
			cmd.addArgs("--force");
		if (quiet)
			cmd.addArgs("--quiet");
		
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
				logger.warn(line);
			}
			
		}).checkReturnCode();
		
		return null;
	}

}
