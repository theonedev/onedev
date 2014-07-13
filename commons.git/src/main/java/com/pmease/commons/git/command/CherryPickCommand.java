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

public class CherryPickCommand extends GitCommand<String> {

	private static final Logger logger = LoggerFactory.getLogger(CherryPickCommand.class);
	
    private String revisions;
    
	public CherryPickCommand(final File repoDir) {
		super(repoDir);
	}

	public CherryPickCommand revisions(String revisions) {
	    this.revisions = revisions;
	    return this;
	}
	
	@Override
	public String call() {
	    Preconditions.checkNotNull(revisions, "revisions has to be specified.");
	    
		Commandline cmd = cmd().addArgs("cherry-pick");
		cmd.addArgs("--no-merges", "--ff", "--quiet");
		
		cmd.addArgs(revisions);

		final AtomicBoolean emptyCommitSet = new AtomicBoolean(false);
		final AtomicBoolean hasConflict = new AtomicBoolean(false);

		ExecuteResult result = cmd.execute(new LineConsumer() {

			@Override
			public void consume(String line) {
				logger.debug(line);
			}
			
		}, new LineConsumer() {

			@Override
			public void consume(String line) {
				if (line.equals("fatal: empty commit set passed")) {
					emptyCommitSet.set(true);
				} else if (line.equals("nothing to commit, working directory clean") 
						|| line.equals("hint: after resolving the conflicts, mark the corrected paths")
						|| line.startsWith("The previous cherry-pick is now empty")) {
					hasConflict.set(true);
				} else {
					logger.error(line);
				}
			}
			
		});
		
		if (emptyCommitSet.get())
			return new Git(repoDir).parseRevision("HEAD", true);
		
		if (hasConflict.get())
			return null;

		result.checkReturnCode();
		
		return new Git(repoDir).parseRevision("HEAD", true);
	}

}
