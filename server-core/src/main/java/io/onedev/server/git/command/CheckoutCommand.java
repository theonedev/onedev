package io.onedev.server.git.command;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import io.onedev.commons.utils.command.Commandline;
import io.onedev.commons.utils.command.LineConsumer;

public class CheckoutCommand extends GitCommand<Void> {

	private static final Logger logger = LoggerFactory.getLogger(CheckoutCommand.class);
	
    private String refspec;
    
	public CheckoutCommand(final File gitDir) {
		super(gitDir);
	}

	public CheckoutCommand refspec(String refspec) {
		this.refspec = refspec;
		return this;
	}

	@Override
	public Void call(Logger logger) {
	    Preconditions.checkNotNull(refspec, "refspec param has to be specified.");
	    
		Commandline cmd = cmd().addArgs("checkout", "--quiet", refspec);
		
		Logger effectiveLogger = logger!=null?logger:CheckoutCommand.logger;
		cmd.execute(new LineConsumer() {

			@Override
			public void consume(String line) {
				effectiveLogger.trace(line);
			}
			
		}, new LineConsumer() {

			@Override
			public void consume(String line) {
				effectiveLogger.error(line);
			}
			
		}, logger).checkReturnCode();
		
		return null;
	}

}
