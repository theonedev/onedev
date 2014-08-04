package com.pmease.commons.git.command;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pmease.commons.util.execution.Commandline;
import com.pmease.commons.util.execution.LineConsumer;

public class RemoveCommand extends GitCommand<Void> {

	private static final Logger logger = LoggerFactory.getLogger(RemoveCommand.class);
	
	private String paths[] = new String[0];
	
	public RemoveCommand(File repoDir) {
		super(repoDir);
	}
	
	public RemoveCommand paths(String... paths) {
		this.paths = paths;
		return this;
	}

	@Override
	public Void call() {
		Commandline cmd = cmd().addArgs("rm");
		
		for (String path: paths) {
			cmd.addArgs(path);
		}
		
		cmd.execute(new LineConsumer() {

			@Override
			public void consume(String line) {
				logger.debug(line);
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
