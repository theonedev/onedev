package com.pmease.commons.git.command;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pmease.commons.util.execution.Commandline;
import com.pmease.commons.util.execution.LineConsumer;

public class InitCommand extends GitCommand<Void> {

	private static final Logger logger = LoggerFactory.getLogger(InitCommand.class);
	
	private boolean bare;
	
	public InitCommand(File repoDir) {
		super(repoDir);
	}
	
	public InitCommand bare(boolean bare) {
		this.bare = bare;
		return this;
	}

	@Override
	public Void call() {
		Commandline cmd = cmd().addArgs("init");
		
		if (bare)
			cmd.addArgs("--bare");
		
		cmd.execute(new LineConsumer() {

			@Override
			public void consume(String line) {
				logger.debug(line);
			}
			
		}, new LineConsumer() {

			@Override
			public void consume(String line) {
				logger.debug(line);
			}
			
		}).checkReturnCode();
		
		return null;
	}

}
