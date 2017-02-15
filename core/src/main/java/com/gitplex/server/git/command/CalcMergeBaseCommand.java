package com.gitplex.server.git.command;

import java.io.File;

import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gitplex.server.util.execution.Commandline;
import com.gitplex.server.util.execution.LineConsumer;
import com.google.common.base.Preconditions;

public class CalcMergeBaseCommand extends GitCommand<String> {

	private static final Logger logger = LoggerFactory.getLogger(CalcMergeBaseCommand.class);
	
	private String rev1;
	
	private String rev2;
	
	public CalcMergeBaseCommand(final File repoDir) {
		super(repoDir);
	}
	
	public CalcMergeBaseCommand rev1(final String rev1) {
		this.rev1 = rev1;
		return this;
	}
	
	public CalcMergeBaseCommand rev2(final String rev2) {
		this.rev2 = rev2;
		return this;
	}
	
	@Override
	public @Nullable String call() {
		Preconditions.checkNotNull(rev1, "rev1 has to be specified.");
		Preconditions.checkNotNull(rev2, "rev2 has to be specified.");
		
		final String commonAncestor[] = new String[]{null};
		
		Commandline cmd = cmd();
		
		cmd.addArgs("merge-base", rev1, rev2);
		
		cmd.execute(new LineConsumer() {

			@Override
			public void consume(String line) {
				if (line.trim().length() != 0)
					commonAncestor[0] = line.trim();
			}
			
		}, new LineConsumer() {

			@Override
			public void consume(String line) {
				logger.error(line);
			}
			
		}).checkReturnCode();
		
		return commonAncestor[0];
	}

}
