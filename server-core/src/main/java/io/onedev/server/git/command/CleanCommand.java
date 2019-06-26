package io.onedev.server.git.command;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.onedev.commons.utils.command.Commandline;
import io.onedev.commons.utils.command.LineConsumer;

public class CleanCommand extends GitCommand<Void> {

	private static final Logger logger = LoggerFactory.getLogger(CleanCommand.class);
	
	private String[] options;
	
	public CleanCommand(final File gitDir) {
		super(gitDir);
	}
	
	public CleanCommand options(String...options) {
		this.options = options;
		return this;
	}

	@Override
	public Void call() {
		Commandline cmd = cmd().addArgs("clean");

		if (options != null)
			cmd.addArgs(options);
		
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
