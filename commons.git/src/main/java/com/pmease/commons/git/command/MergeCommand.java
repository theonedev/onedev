package com.pmease.commons.git.command;

import java.io.File;

import com.google.common.base.Preconditions;
import com.pmease.commons.util.execution.Commandline;
import com.pmease.commons.util.execution.ExecuteResult;
import com.pmease.commons.util.execution.LineConsumer;

public class MergeCommand extends GitCommand<Boolean> {

    private String revision;
    
    private String strategy;
    
    private String strategyOption;
    
    private String message;
    
	public MergeCommand(final File repoDir) {
		super(repoDir);
	}

	public MergeCommand revision(String revision) {
	    this.revision = revision;
	    return this;
	}
	
	public MergeCommand strategy(String strategy) {
		this.strategy = strategy;
		return this;
	}
	
	public MergeCommand strategyOption(String strategyOption) {
		this.strategyOption = strategyOption;
		return this;
	}
	
	public MergeCommand message(String message) {
		this.message = message;
		return this;
	}
	
	@Override
	public Boolean call() {
	    Preconditions.checkNotNull(revision, "revision has to be specified.");
	    
		Commandline cmd = cmd().addArgs("merge");
		if (strategy != null)
			cmd.addArgs("-s", strategy);
		if (strategyOption != null)
			cmd.addArgs("-X", strategyOption);
		
		cmd.addArgs("--no-edit", "--log", "--quiet");
		if (message != null)
			cmd.addArgs("-m", message);
		
		cmd.addArgs(revision);

		final boolean conflict[] = new boolean[]{false};

		ExecuteResult result = cmd.execute(new LineConsumer() {

			@Override
			public void consume(String line) {
				if (line.startsWith("CONFLICT"))
					conflict[0] = true;
			}
			
		}, errorLogger);
		
		if (conflict[0])
			return false;
		
		result.checkReturnCode();
		
		return true;
	}

}
