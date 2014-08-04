package com.pmease.commons.git.command;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pmease.commons.util.execution.Commandline;
import com.pmease.commons.util.execution.LineConsumer;

public class AddCommand extends GitCommand<Void> {

	private static final Logger logger = LoggerFactory.getLogger(AddCommand.class);
	
	private String paths[] = new String[0];
	
	public AddCommand(File repoDir) {
		super(repoDir);
	}
	
	public AddCommand addPaths(String... paths) {
		this.paths = paths;
		return this;
	}

	@Override
	public Void call() {
		Commandline cmd = cmd().addArgs("add");
		
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
				if (line.startsWith("warning: "))
					logger.warn(line.substring("warning: ".length()));
				else if (line.startsWith("The file will have its original line endings"))
					logger.warn(line);
				else if (line.startsWith("The file will have its original line endings in your working directory"))
					logger.warn(line);
				else
					logger.error(line);
			}
			
		}).checkReturnCode();
		
		return null;
	}

}
