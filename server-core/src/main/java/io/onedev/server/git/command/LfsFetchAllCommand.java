package io.onedev.server.git.command;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.onedev.commons.utils.command.Commandline;
import io.onedev.commons.utils.command.LineConsumer;
import io.onedev.server.git.CommandUtils;

public class LfsFetchAllCommand {

	private static final Logger logger = LoggerFactory.getLogger(LfsFetchAllCommand.class);
	
	private final File workingDir;
	
	public LfsFetchAllCommand(File workingDir) {
		this.workingDir = workingDir;
	}

	protected Commandline newGit() {
		return CommandUtils.newGit();
	}
	
	public void run() {
		Commandline git = newGit().workingDir(workingDir).addArgs("lfs", "fetch", "--all");
		
		git.execute(new LineConsumer() {

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
	}

}
