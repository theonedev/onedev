package io.onedev.server.git.command;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.onedev.commons.utils.command.Commandline;
import io.onedev.commons.utils.command.LineConsumer;

public class LfsFetchAllCommand extends GitCommand<Void> {

	private static final Logger logger = LoggerFactory.getLogger(LfsFetchAllCommand.class);
	
	public LfsFetchAllCommand(File gitDir) {
		super(gitDir);
	}

	@Override
	public Void call() {
		Commandline cmd = cmd().addArgs("lfs", "fetch", "--all");
		
		cmd.execute(new LineConsumer() {

			@Override
			public void consume(String line) {
				logger.trace(line);
			}
			
		}, new LineConsumer(){

			@Override
			public void consume(String line) {
				logger.error(line);
			}
			
		}).checkReturnCode();
		
		return null;
	}

}
