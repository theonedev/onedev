package com.pmease.commons.git.command;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.pmease.commons.util.execution.Commandline;
import com.pmease.commons.util.execution.LineConsumer;

public class AddSubModuleCommand extends GitCommand<Void> {

	private static final Logger logger = LoggerFactory.getLogger(AddSubModuleCommand.class);
	
	private String url;
	
	private String path;
	
	public AddSubModuleCommand(File repoDir) {
		super(repoDir);
	}
	
	public AddSubModuleCommand url(String url) {
		this.url = url;
		return this;
	}
	
	public AddSubModuleCommand path(String path) {
		this.path = path;
		return this;
	}

	@Override
	public Void call() {
		Preconditions.checkNotNull(url, "url should be specified.");
		Preconditions.checkNotNull(path, "path should be specified.");
		
		Commandline cmd = cmd().addArgs("submodule", "add", url, path);
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
				else if (line.startsWith("Cloning into"))
					logger.info(line);
				else if (line.equals("done."))
					logger.info(line);
				else
					logger.error(line);
			}
			
		}).checkReturnCode();
		
		return null;
	}

}
