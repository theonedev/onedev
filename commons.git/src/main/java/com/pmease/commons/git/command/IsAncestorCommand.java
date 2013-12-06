package com.pmease.commons.git.command;

import java.io.File;

import com.google.common.base.Preconditions;
import com.pmease.commons.util.execution.Commandline;
import com.pmease.commons.util.execution.ExecuteResult;
import com.pmease.commons.util.execution.LineConsumer;

public class IsAncestorCommand extends GitCommand<Boolean> {

	private String ancestor;
	
	private String descendant;
	
	public IsAncestorCommand(final File repoDir) {
		super(repoDir);
	}
	
	public IsAncestorCommand ancestor(final String ancestor) {
		this.ancestor = ancestor;
		return this;
	}
	
	public IsAncestorCommand descendant(final String descendant) {
		this.descendant = descendant;
		return this;
	}
	
	@Override
	public Boolean call() {
		Preconditions.checkNotNull(ancestor, "ancestor has to be specified.");
		Preconditions.checkNotNull(descendant, "descendant has to be specified.");
		
		Commandline cmd = cmd();
		
		cmd.addArgs("merge-base", "--is-ancestor", ancestor, descendant);
		
		final boolean revExists[] = new boolean[]{true};
		
		ExecuteResult result = cmd.execute(infoLogger, new LineConsumer() {

			@Override
			public void consume(String line) {
				error(line);
				if (line.startsWith("fatal: Not a valid object name")) {
					revExists[0] = false;
				}
			}
			
		});
		
		if (!revExists[0])
			return false;
		
		if (result.getReturnCode() == 0)
			return true;
		else if (result.getReturnCode() == 1)
			return false;
		else
			throw result.buildException();
	}

}
