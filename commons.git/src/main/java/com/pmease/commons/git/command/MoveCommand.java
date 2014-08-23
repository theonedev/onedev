package com.pmease.commons.git.command;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.pmease.commons.util.execution.Commandline;
import com.pmease.commons.util.execution.LineConsumer;

public class MoveCommand extends GitCommand<Void> {

	private static final Logger logger = LoggerFactory.getLogger(MoveCommand.class);
	
	private String source;
	
	private String destination;
	
	public MoveCommand(File repoDir) {
		super(repoDir);
	}
	
	public MoveCommand source(String source) {
		this.source = source;
		return this;
	}
	
	public MoveCommand destination(String destination) {
		this.destination = destination;
		return this;
	}

	@Override
	public Void call() {
		Preconditions.checkNotNull(source, "source has to be specified.");
		Preconditions.checkNotNull(destination, "destination has to be specified.");
		
		Commandline cmd = cmd().addArgs("mv", source, destination);
		
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
