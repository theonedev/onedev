package com.pmease.commons.git.command;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pmease.commons.util.execution.Commandline;
import com.pmease.commons.util.execution.LineConsumer;

public class ResetCommand extends GitCommand<Void> {

	private static final Logger logger = LoggerFactory.getLogger(ResetCommand.class);
	
	private String commit;
	
	private String mode;
	
	public ResetCommand(File repoDir) {
		super(repoDir);
	}

	public ResetCommand commit(String commit) {
		this.commit = commit;
		return this;
	}
	
	public ResetCommand mode(String mode) {
		this.mode = mode;
		return this;
	}
	
	@Override
	public Void call() {
		Commandline cmd = cmd().addArgs("reset");
		cmd.addArgs("--quiet");
		if (mode != null)
			cmd.addArgs("mode");
		if (commit != null)
			cmd.addArgs(commit);
		
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
