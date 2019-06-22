package io.onedev.server.git.command;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import io.onedev.commons.utils.command.Commandline;
import io.onedev.commons.utils.command.LineConsumer;

public class InitCommand extends GitCommand<Void> {

	private static final Logger logger = LoggerFactory.getLogger(InitCommand.class);
	
    private String from;
    
    private String[] refspec = new String[0];
    
	public InitCommand(final File gitDir) {
		super(gitDir);
	}

	public InitCommand from(String from) {
	    this.from = from;
	    return this;
	}
	
	public InitCommand refspec(String... refspec) {
		this.refspec = refspec;
		return this;
	}
	
	@Override
	public Void call(Logger logger) {
	    Preconditions.checkNotNull(from, "from param has to be specified.");
	    
		Commandline cmd = cmd().addArgs("fetch");
		cmd.addArgs(from);
		cmd.addArgs("--force");
		
		for (String each: refspec)
			cmd.addArgs(each);
		
		Logger effectiveLogger = logger!=null?logger:InitCommand.logger;
		cmd.execute(new LineConsumer() {

			@Override
			public void consume(String line) {
				effectiveLogger.trace(line);
			}
			
		}, new LineConsumer() {

			@Override
			public void consume(String line) {
				if (line.startsWith("From ") 
						|| line.startsWith(" * branch") 
						|| line.startsWith(" * [new ref]") 
						|| line.contains("..") && line.contains("->")) {
					effectiveLogger.info(line);
				} else {
					effectiveLogger.error(line);
				}
			}
			
		}, logger).checkReturnCode();
		
		return null;
	}

}
