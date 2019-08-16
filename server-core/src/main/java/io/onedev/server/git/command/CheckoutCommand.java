package io.onedev.server.git.command;

import java.io.File;
import java.util.Map;

import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import io.onedev.commons.utils.command.Commandline;
import io.onedev.commons.utils.command.LineConsumer;

public class CheckoutCommand extends GitCommand<Void> {

	private static final Logger logger = LoggerFactory.getLogger(CheckoutCommand.class);
	
    private String refspec;
    
	public CheckoutCommand(File gitDir, @Nullable Map<String, String> environments) {
		super(gitDir, environments);
	}

	public CheckoutCommand refspec(String refspec) {
		this.refspec = refspec;
		return this;
	}

	@Override
	public Void call() {
	    Preconditions.checkNotNull(refspec, "refspec param has to be specified.");
	    
		Commandline cmd = cmd().addArgs("checkout", "--quiet", refspec);
		
		cmd.execute(new LineConsumer() {

			@Override
			public void consume(String line) {
				logger.trace(line);
			}
			
		}, new LineConsumer() {

			@Override
			public void consume(String line) {
				logger.error(line);
			}
			
		}).checkReturnCode();
		
		return null;
	}

}
