package com.pmease.commons.git.command;

import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.pmease.commons.git.Git;
import com.pmease.commons.util.execution.Commandline;
import com.pmease.commons.util.execution.ExecuteResult;
import com.pmease.commons.util.execution.LineConsumer;

public class MergeCommand extends GitCommand<String> {

	private static final Logger logger = LoggerFactory.getLogger(MergeCommand.class);
	
	public enum FastForwardMode {FF_ONLY, NO_FF, FF};
	
    private String revision;
    
    private FastForwardMode fastForwardMode;
    
    private boolean squash;
    
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
	
	public MergeCommand fastForwardMode(FastForwardMode fastForwardMode) {
		this.fastForwardMode = fastForwardMode;
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
	
	public MergeCommand squash(boolean squash) {
		this.squash = squash;
		return this;
	}

	public MergeCommand message(String message) {
		this.message = message;
		return this;
	}
	
	@Override
	public String call() {
	    Preconditions.checkNotNull(revision, "revision has to be specified.");
	    
		Commandline cmd = cmd().addArgs("merge");
		
		cmd.addArgs("--no-edit", "--quiet");
		
		if (fastForwardMode == FastForwardMode.FF_ONLY)
			cmd.addArgs("--ff-only");
		else if (fastForwardMode == FastForwardMode.NO_FF)
			cmd.addArgs("--no-ff");
		else
			cmd.addArgs("--ff");
		
		if (strategy != null)
			cmd.addArgs("--strategy=" + strategy);
		if (strategyOption != null)
			cmd.addArgs("--strategy-option=" + strategyOption);
		
		if (squash)
			cmd.addArgs("--squash");
		else if (message != null)
			cmd.addArgs("-m", message);
		
		cmd.addArgs(revision);

		final AtomicBoolean conflict = new AtomicBoolean(false);
		
		ExecuteResult result = cmd.execute(new LineConsumer() {

			@Override
			public void consume(String line) {
				if (line.startsWith("CONFLICT") || line.startsWith("fatal: Not possible to fast-forward"))
					conflict.set(true);
			}
			
		}, new LineConsumer() {

			@Override
			public void consume(String line) {
				logger.error(line);
			}
			
		});
		
		if (conflict.get())
			return null;
		
		result.checkReturnCode();
		
		Git git = new Git(repoDir);
		if (squash) 
			git.commit(message, false, false);
		return git.parseRevision("HEAD", true);
	}

}
