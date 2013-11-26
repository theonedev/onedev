package com.pmease.commons.git.command;

import java.io.File;

import com.google.common.base.Preconditions;
import com.pmease.commons.util.execution.Commandline;

public class CloneCommand extends GitCommand<Void> {

	private String from;
	
	private boolean bare;
	
	private boolean shared;
	
	private boolean noCheckout;
	
	private String branch;
	
	public CloneCommand(File repoDir) {
		super(repoDir);
	}

	public CloneCommand from(String from) {
		this.from = from;
		return this;
	}
	
	public CloneCommand bare(boolean bare) {
		this.bare = bare;
		return this;
	}
	
	public CloneCommand shared(boolean shared) {
		this.shared = shared;
		return this;
	}
	
	public CloneCommand noCheckout(boolean noCheckout) {
		this.noCheckout = noCheckout;
		return this;
	}
	
	public CloneCommand branch(String branch) {
		this.branch = branch;
		return this;
	}
	
	@Override
	public Void call() {
		Preconditions.checkNotNull(from, "from has to be specified.");
		
		Commandline cmd = cmd().addArgs("clone");
		if (bare)
			cmd.addArgs("--bare");
		if (shared) 
			cmd.addArgs("--shared");
		if (noCheckout) 
			cmd.addArgs("--no-checkout");
		if (branch != null)
			cmd.addArgs("-b", branch);
		
		cmd.addArgs(from);
		cmd.addArgs(".");
		
		cmd.execute(debugLogger(), errorLogger()).checkReturnCode();
		
		return null;
	}

}
