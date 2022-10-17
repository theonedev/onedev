package io.onedev.server.git.command;

import java.io.File;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.onedev.commons.utils.command.Commandline;
import io.onedev.commons.utils.command.ExecutionResult;
import io.onedev.commons.utils.command.LineConsumer;
import io.onedev.server.git.CommandUtils;

public class IsAncestorCommand {

	private static final Logger logger = LoggerFactory.getLogger(IsAncestorCommand.class);
	
	private final File workingDir;
	
	private final String ancestor;
	
	private final String descendant;
	
	private final Map<String, String> envs;
	
	public IsAncestorCommand(File workingDir, String ancestor, String descendant, 
			Map<String, String> envs) {
		this.workingDir = workingDir;
		this.ancestor = ancestor;
		this.descendant = descendant;
		this.envs = envs;
	}
	
	protected Commandline newGit() {
		return CommandUtils.newGit();
	}
	
	public boolean run() {
		Commandline git = newGit().workingDir(workingDir);
		git.environments().putAll(envs);
		
		git.addArgs("merge-base", "--is-ancestor", ancestor, descendant);
		
		ExecutionResult result = git.execute(new LineConsumer() {

			@Override
			public void consume(String line) {
			}
			
		}, new LineConsumer() {

			@Override
			public void consume(String line) {
				logger.error(line);
			}
			
		});
		
		if (result.getReturnCode() == 0)
			return true;
		else if (result.getReturnCode() == 1)
			return false;
		else
			throw result.buildException();
	}

}