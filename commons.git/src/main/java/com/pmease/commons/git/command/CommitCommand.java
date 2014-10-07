package com.pmease.commons.git.command;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.pmease.commons.util.execution.Commandline;
import com.pmease.commons.util.execution.LineConsumer;

public class CommitCommand extends GitCommand<Void> {

	private static final Logger logger = LoggerFactory.getLogger(CommitCommand.class);
	
	private String message;
	
	private boolean amend;
	
	private boolean add;
	
	public CommitCommand(final File repoDir) {
		super(repoDir);
	}
	
	public CommitCommand message(final String message) {
		this.message = message;
		return this;
	}
	
	public CommitCommand amend(final boolean amend) {
		this.amend = amend;
		return this;
	}
	
	public CommitCommand add(final boolean add) {
		this.add = add;
		return this;
	}
	
	@Override
	public Void call() {
		Preconditions.checkNotNull(message, "Commit message has to be specified.");
		
		Commandline cmd = cmd().addArgs("commit");
		
		cmd.addArgs("-m", message);
		if (add)
			cmd.addArgs("-a");
		if (amend) 
			cmd.addArgs("--amend");
		
		cmd.execute(new LineConsumer() {

			@Override
			public void consume(String line) {
				if (line.startsWith("no changes added to commit"))
					logger.error(line);
				else
					logger.debug(line);
			}
			
		}, new LineConsumer() {

			@Override
			public void consume(String line) {
				System.err.println(line);
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
