package com.gitplex.server.git.command;

import java.io.File;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gitplex.utils.command.Commandline;
import com.gitplex.utils.command.ExecuteResult;
import com.gitplex.utils.command.LineConsumer;
import com.google.common.base.Preconditions;

public class IsAncestorCommand extends GitCommand<Boolean> {

	private static final Logger logger = LoggerFactory.getLogger(IsAncestorCommand.class);
	
	private String ancestor;
	
	private String descendant;
	
	public IsAncestorCommand(File gitDir, Map<String, String> gitEnvs) {
		super(gitDir, gitEnvs);
	}
	
	public IsAncestorCommand ancestor(String ancestor) {
		this.ancestor = ancestor;
		return this;
	}
	
	public IsAncestorCommand descendant(String descendant) {
		this.descendant = descendant;
		return this;
	}
	
	@Override
	public Boolean call() {
		Preconditions.checkNotNull(ancestor, "ancestor has to be specified.");
		Preconditions.checkNotNull(descendant, "descendant has to be specified.");
		
		Commandline cmd = cmd();
		
		cmd.addArgs("merge-base", "--is-ancestor", ancestor, descendant);
		
		ExecuteResult result = cmd.execute(new LineConsumer() {

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