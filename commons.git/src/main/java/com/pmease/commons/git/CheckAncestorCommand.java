package com.pmease.commons.git;

import com.google.common.base.Preconditions;
import com.pmease.commons.util.execution.Commandline;
import com.pmease.commons.util.execution.ExecuteResult;

public class CheckAncestorCommand extends GitCommand<Boolean> {

	private String ancestor;
	
	private String descendant;
	
	public CheckAncestorCommand(final Git git) {
		super(git);
	}
	
	public CheckAncestorCommand ancestor(final String ancestor) {
		this.ancestor = ancestor;
		return this;
	}
	
	public CheckAncestorCommand descendant(final String descendant) {
		this.descendant = descendant;
		return this;
	}
	
	@Override
	public Boolean call() {
		Preconditions.checkNotNull(ancestor, "ancestor has to be specified.");
		Preconditions.checkNotNull(descendant, "descendant has to be specified.");
		
		Commandline cmd = git().cmd();
		
		cmd.addArgs("merge-base", "--is-ancestor", ancestor, descendant);
		
		ExecuteResult result = cmd.execute(infoLogger(), errorLogger());
		
		if (result.getReturnCode() == 0)
			return true;
		else if (result.getReturnCode() == 1)
			return false;
		else
			throw result.buildException();
	}

}
