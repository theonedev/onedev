package com.pmease.commons.git.command;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.pmease.commons.util.execution.Commandline;
import com.pmease.commons.util.execution.LineConsumer;

public class AddNoteCommand extends GitCommand<Void> {

	private static final Logger logger = LoggerFactory.getLogger(AddNoteCommand.class);
	
	private String object;
	
	private String message;
	
	public AddNoteCommand(File repoDir) {
		super(repoDir);
	}
	
	public AddNoteCommand object(String object) {
		this.object = object;
		return this;
	}
	
	public AddNoteCommand message(String message) {
		this.message = message;
		return this;
	}

	@Override
	public Void call() {
		Preconditions.checkNotNull(object, "object should be specified.");
		Preconditions.checkNotNull(message, "message should be specified.");
		Commandline cmd = cmd().addArgs("notes", "add", "-m", message, object);
		
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
